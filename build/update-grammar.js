/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *--------------------------------------------------------------------------------------------*/

/**
 * MIT License
 * 
 * Copyright (c) 2015 - present Microsoft Corporation
 * 
 * All rights reserved.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

'use strict';

var path = require('path');
var fs = require('fs');
var plist = require('fast-plist');
var cson = require('cson-parser');
var https = require('https');
var url = require('url');

/**
 * @param {string} urlString
 */
function getOptions(urlString) {
    var _url = url.parse(urlString);
    var headers = {
        'User-Agent': 'VSCode'
    };
    var token = process.env['GITHUB_TOKEN'];
    if (token) {
        headers['Authorization'] = 'token ' + token;
    }
    return {
        protocol: _url.protocol,
        host: _url.host,
        port: _url.port,
        path: _url.path,
        headers: headers
    };
}

/**
 * @param {string} url
 * @param {number} redirectCount
 */
function download(url, redirectCount) {
    return new Promise((c, e) => {
        var content = '';
        https.get(getOptions(url), function (response) {
            response.on('data', function (data) {
                content += data.toString();
            }).on('end', function () {
                if (response.statusCode === 403 && response.headers['x-ratelimit-remaining'] === '0') {
                    e('GitHub API rate exceeded. Set GITHUB_TOKEN environment variable to increase rate limit.');
                    return;
                }
                let count = redirectCount || 0;
                if (count < 5 && response.statusCode >= 300 && response.statusCode <= 303 || response.statusCode === 307) {
                    let location = response.headers['location'];
                    if (location) {
                        console.log("Redirected " + url + " to " + location);
                        download(location, count + 1).then(c, e);
                        return;
                    }
                }
                c(content);
            });
        }).on('error', function (err) {
            e(err.message);
        });
    });
}

function getCommitSha(repoId, repoPath) {
    var commitInfo = 'https://api.github.com/repos/' + repoId + '/commits?path=' + repoPath;
    return download(commitInfo).then(function (content) {
        try {
            let lastCommit = JSON.parse(content)[0];
            return Promise.resolve({
                commitSha: lastCommit.sha,
                commitDate: lastCommit.commit.author.date
            });
        } catch (e) {
            return Promise.reject(new Error("Failed extracting the SHA: " + content));
        }
    });
}

exports.update = function (repoId, repoPath, dest, modifyGrammar, version = 'calva') {
    var contentPath = 'https://raw.githubusercontent.com/' + repoId + `/${version}/` + repoPath;
    console.log('Reading from ' + contentPath);
    return download(contentPath).then(function (content) {
        var ext = path.extname(repoPath);
        var grammar;
        if (ext === '.tmLanguage' || ext === '.plist') {
            grammar = plist.parse(content);
        } else if (ext === '.cson') {
            grammar = cson.parse(content);
        } else if (ext === '.json' || ext === '.JSON-tmLanguage') {
            grammar = JSON.parse(content);
        } else {
            return Promise.reject(new Error('Unknown file extension: ' + ext));
        }
        if (modifyGrammar) {
            modifyGrammar(grammar);
        }
        return getCommitSha(repoId, repoPath).then(function (info) {
            let result = {
                information_for_contributors: [
                    'This file has been converted from https://github.com/' + repoId + '/blob/calva/' + repoPath,
                    'If you want to provide a fix or improvement, please create a pull request there.'
                ]
            };

            if (info) {
                result.version = 'https://github.com/' + repoId + '/commit/' + info.commitSha;
            }

            let keys = ['name', 'scopeName', 'comment', 'injections', 'patterns', 'repository'];
            for (let key of keys) {
                if (grammar.hasOwnProperty(key)) {
                    result[key] = grammar[key];
                }
            }

            try {
                fs.writeFileSync(dest, JSON.stringify(result, null, '\t').replace(/\n/g, '\r\n'));
                // Add commit sha to cgmanifest
                let cgmanifestRead = JSON.parse(fs.readFileSync('./cgmanifest.json').toString());
                let promises = new Array();
                let packageJsonPath = 'https://raw.githubusercontent.com/' + repoId + `/${info.commitSha}/package.json`;
                for (let i = 0; i < cgmanifestRead.registrations.length; i++) {
                    if (cgmanifestRead.registrations[i].component.git.repositoryUrl.substr(cgmanifestRead.registrations[i].component.git.repositoryUrl.length - repoId.length, repoId.length) === repoId) {
                        cgmanifestRead.registrations[i].component.git.commitHash = info.commitSha;
                        promises.push(download(packageJsonPath).then(function (packageJson) {
                            if (packageJson) {
                                try {
                                    cgmanifestRead.registrations[i].version = JSON.parse(packageJson).version;
                                } catch (e) {
                                    console.log('File does not exist at' + packageJsonPath);
                                }
                            }
                        }));
                        break;
                    }
                }
                Promise.all(promises).then(function (allResult) {
                    fs.writeFileSync('./cgmanifest.json', JSON.stringify(cgmanifestRead, null, '\t').replace(/\n/g, '\r\n'));
                });
                if (info) {
                    console.log('Updated ' + path.basename(dest) + ' to ' + repoId + '@' + info.commitSha.substr(0, 7) + ' (' + info.commitDate.substr(0, 10) + ')');
                } else {
                    console.log('Updated ' + path.basename(dest));
                }
            } catch (e) {
                return Promise.reject(e);
            }
        });

    }, console.error).catch(e => {
        console.error(e);
        process.exit(1);
    });
};

if (path.basename(process.argv[1]) === 'update-grammar.js') {
    for (var i = 3; i < process.argv.length; i += 2) {
        exports.update(process.argv[2], process.argv[i], process.argv[i + 1]);
    }
}
