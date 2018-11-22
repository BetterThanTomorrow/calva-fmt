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

interface InferOptions {
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
        r: InferOptions = calvaFmtLib.inferParens({
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