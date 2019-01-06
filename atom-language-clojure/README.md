# Calva Syntax Highlight Grammar

Calva Formats's `clojure.tmLanguage.json` file is built from here `atom-language-clojure/grammars/clojure.cson`. After making changes there, also update `spec/clojure-spec.coffee` and make sure all tests pass.

To run the tests you need to open this directory in Atom and issue the **Run Package Specs** command.

When all old and new tests pass, update Calva Format's grammar from the root of the `calva-fmt` project:

```sh
$ npm run update-grammar
```
