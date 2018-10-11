# Change Log
All notable changes to the "calva-fmt" extension will be documented in this file. (At least when I remember to update it.)

## Under Consideration
### Planning
- Support for special formatting on a per-symbol basis.
- Support for honoring the `:cljfmt` setting in Leinigen projects.

### Thinking About
- Keep the file formatted as you type. Always.

## Work in pogress
- Handle reformat current range when on empty lines better and smarter.

## 0.0.9 - 2018-10-05
- Add user command for indenting current form

## 0.0.5 - 2018-07-18
- Expose `formatRange` in extension API.
- Expose `formatPosition` (formatting the current form based on cursor position) in extension API.

## 0.0.4 - 2018-06-23
- Use `clj-fmt` auto-adjust of cursor position on new lines.

## 0.0.3 - 2018-06-20
- Calva Formatter is Calva's formatter.

## 0.0.2 - 2018-06-07
- Adjust cursor position on new lines as you type (using custom code).

## 0.0.1 - 2018-05-27
- Initial release.
- Format Selection.
- Fomrat on Save.