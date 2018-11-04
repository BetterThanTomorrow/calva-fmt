# Change Log
All notable changes to the "calva-fmt" extension will be documented in this file. (At least when I remember to update it.)

## Future

### Thinking About
- Keep the file formatted as you type. Always.

### Work in pogress
- Support for honoring the `:cljfmt` setting in Leinigen projects.

## 0.0.15 - 2018-11-04
- Format code as new lines are entered, keeping things mostly formatted as you type

## 0.0.14 - 2018-10-11
- Reformat current form works so nicely that it gets back the default key binding to `tab`

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