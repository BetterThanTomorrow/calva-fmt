import * as vscode from 'vscode';
import * as calvaFmtLib from '../lib/calva_fmt';


interface CFEdit {
    edit: string,
    start: { line: number, character: number },
    end: { line: number, character: number },
    text?: string
}

interface CFError {
    message: string
}

interface ResultOptions {
    success: boolean,
    edits?: [CFEdit],
    line?: number,
    character?: number,
    error?: CFError,
    "error-msg"?: string
}

export function inferParensCommand(editor: vscode.TextEditor) {
    const position: vscode.Position = editor.selection.active,
        document = editor.document,
        currentText = document.getText(),
        r: ResultOptions = calvaFmtLib.inferParens({
            "text": currentText,
            "line": position.line,
            "character": position.character
        });
    if (r.success) {
        editor.edit(editBuilder => {
            r.edits.forEach((edit: CFEdit) => {
                const start = new vscode.Position(edit.start.line, edit.start.character),
                    end = new vscode.Position(edit.end.line, edit.end.character);
                editBuilder.replace(new vscode.Range(start, end), edit.text);
            });
        }, { undoStopAfter: true, undoStopBefore: false }).then((_onFulfilled: boolean) => {
            const newPosition = new vscode.Position(r.line, r.character);
            editor.selections = [new vscode.Selection(newPosition, newPosition)];
        });
    }
}

export function tabIndentCommand(editor: vscode.TextEditor, tab: string, forward: boolean = true) {
    const prevPosition: vscode.Position = editor.selection.active,
        document = editor.document;
    let deletedText = "",
        doEdit = true;

    editor.edit(editBuilder => {
        if (forward) {
            editBuilder.insert(new vscode.Position(prevPosition.line, prevPosition.character), tab)
        } else {
            const startOfLine = new vscode.Position(prevPosition.line, 0),
                headRange = new vscode.Range(startOfLine, prevPosition),
                headText = document.getText(headRange),
                xOfFirstLeadingSpace = headText.search(/ *$/),
                leadingSpaces = xOfFirstLeadingSpace >= 0 ? prevPosition.character - xOfFirstLeadingSpace : 0;
            if (leadingSpaces > 0) {
                const tabSize = Math.max(tab.length, 1),
                    deleteRange = new vscode.Range(prevPosition.translate(0, -tabSize), prevPosition);
                deletedText = document.getText(deleteRange);
                editBuilder.delete(deleteRange);
            } else {
                doEdit = false;
            }
        }
    }, { undoStopAfter: false, undoStopBefore: false }).then((_onFulfilled: boolean) => {
        const position: vscode.Position = editor.selection.active,
            currentText = document.getText();
        if (doEdit) {
            const r: ResultOptions = calvaFmtLib.inferIndents({
                "text": currentText,
                "line": position.line,
                "character": position.character,
                "previous-line": prevPosition.line,
                "previous-character": prevPosition.character,
                "changes": [{
                    "line": forward ? prevPosition.line : position.line,
                    "character": forward ? prevPosition.character : position.character,
                    "old-text": forward ? "" : deletedText,
                    "new-text": forward ? tab : ""
                }]
            });
            if (r.success) {
                editor.edit(editBuilder => {
                    r.edits.forEach((edit: CFEdit) => {
                        const start = new vscode.Position(edit.start.line, edit.start.character),
                            end = new vscode.Position(edit.end.line, edit.end.character);
                        editBuilder.replace(new vscode.Range(start, end), edit.text);
                    });
                }, { undoStopAfter: true, undoStopBefore: false }).then((_onFulfilled: boolean) => {
                    const newPosition = new vscode.Position(r.line, r.character);
                    editor.selections = [new vscode.Selection(newPosition, newPosition)];
                });
            } else {
                vscode.window.showErrorMessage("Calva Formatter Error: " + (r.error ? r.error.message : r["error-msg"]));
            }
        }
    })
}