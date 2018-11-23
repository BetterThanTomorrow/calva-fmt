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
    error?: CFError
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

export function tabIndentCommand(editor: vscode.TextEditor) {
    const tabSize = 1,
        tab = " ".repeat(tabSize),
        prevPosition: vscode.Position = editor.selection.active;
    editor.edit(editBuilder => {
        editBuilder.insert(new vscode.Position(prevPosition.line, prevPosition.character), tab)
    }, { undoStopAfter: false, undoStopBefore: false }).then((_onFulfilled: boolean) => {
        const document = editor.document,
            position: vscode.Position = editor.selection.active,
            currentText = document.getText(),
            r: ResultOptions = calvaFmtLib.inferIndents({
                "text": currentText,
                "line": position.line,
                "character": position.character,
                "previous-line": prevPosition.line,
                "previous-character": prevPosition.character,
                "changes": [{
                    "line": prevPosition.line,
                    "character": prevPosition.character,
                    "old-text": "",
                    "new-text": tab
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
        }
    })
}