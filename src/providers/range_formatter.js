const vscode = require('vscode');
const { formatText, cljify, jsify } = require('../../cljc_out/calva_formatter');

function readConfiguration(configuration) {
    return {
        "indentation?": configuration.get("indentation"),
        "remove-surrounding-whitespace?": configuration.get("removeSurroundingWhitespace"),
        "remove-trailing-whitespace?": configuration.get("removeTrailingWhitespace"),
        "insert-missin-whitespace?": configuration.get("insertMissingWhitespace")
    };
}

class RangeEditProvider {
    provideDocumentRangeFormattingEdits(document, range, options, token) {
        let config = readConfiguration(vscode.workspace.getConfiguration("calva.fmt")),
            text = document.getText(range);

        config["remove-consecutive-blank-lines?"] = false;
        let dict = { "text": text, "config": config };
        let newDict = formatText(cljify(dict));
        newDict = jsify(newDict);
        if (!newDict["error"]) {
            return [vscode.TextEdit.replace(range, newDict["new-text"])];
        }
        else {
            console.log(newDict["error"]);
            return [];
        }
    }
}

module.exports = {
    RangeEditProvider
}