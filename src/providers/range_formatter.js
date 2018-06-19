const vscode = require('vscode');
const formatter = require('../formatter');

class RangeEditProvider {
    provideDocumentRangeFormattingEdits(document, range, options, token) {
        let text = document.getText(range),
            newText = formatter.format(text);
        return [vscode.TextEdit.replace(range, newText)];
    }
}

module.exports = {
    RangeEditProvider
}