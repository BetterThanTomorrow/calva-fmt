import * as vscode from 'vscode';
const { formatText, cljify, jsify, indentForPosition } = require('../cljc_out/calva_fmt');

function readConfiguration() {
    let workspaceConfig = vscode.workspace.getConfiguration("calva.fmt")
    return {
        "indentation?": workspaceConfig.get("indentation"),
        "remove-surrounding-whitespace?": workspaceConfig.get("removeSurroundingWhitespace"),
        "remove-trailing-whitespace?": workspaceConfig.get("removeTrailingWhitespace"),
        "insert-missin-whitespace?": workspaceConfig.get("insertMissingWhitespace")
    };
}

function getConfig() {
    let config = readConfiguration();
    config["remove-consecutive-blank-lines?"] = false; // Until we understand its behaviour better
    return config;
}

export function format(text: string) {
    let d = { "text": text, "config": getConfig() };
    d = formatText(cljify(d));
    d = jsify(d);
    if (!d["error"]) {
        return d["text"];
    }
    else {
        console.log(d["error"]);
        return text;
    }
}

export function calculateIndent(document: vscode.TextDocument, pos: vscode.Position) {
    let range: vscode.Range = new vscode.Range(new vscode.Position(0, 0), new vscode.Position(document.lineCount, Number.MAX_SAFE_INTEGER)),
        text: string = document.getText(range),
        cljRange: object = {
            "start": { "line": range.start.line, "character": range.start.character },
            "end": { "line": range.end.line, "character": range.end.character }
        },
        cljPos: object = { "line": pos.line, "character": pos.character },
        data: object = { "text": text, "range": cljRange, "pos": cljPos, "config": getConfig() },
        result: object = jsify(indentForPosition(cljify(data)));
    if (!result["error"]) {
        return result["indent"];
    } else {
        console.log(result["error"]);
        return pos.character;
    }
}