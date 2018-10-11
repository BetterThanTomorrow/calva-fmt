# calva-fmt

This is the [Calva Formatter](https://marketplace.visualstudio.com/items?itemName=cospaia.calva-fmt) - a Clojure and ClojureScript formatter for Visual Studio Code.

## Raison d´être

1. **Fewer dependencies, less headaches**. You should be able to edit a Clojure file, with full formatting help, without depending on a REPL running or anything else needed to be installed.
1. **Fewer conflicts, more predictability**. As VSCode gets to be a more serious editor for Clojurians there is a an editing war going on between the various plugins that help with editing Clojure code. Calva Formatter is aiming at being the major Clojure formatter, lifting this responsibility from the shoulders of extensions like Calva, Paredit and other Clojure related extensions.

## Features

* Formats according to the community [Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide) plus giving you some options to tweak this style.
* Has a command for formatting the enclosing form, default key binding is `cmd+k tab`.
* Is the formater used for the VSCode *Format Selection* and *Format Document* commands.
* Is intended to be used alongside and by other Clojure extensions.
* Adjusts the cursor position when entering new lines, so that most often you don't have to do this yourself

## How to use

Install it and edit away. Configure it to format the code intentionally or automatically on save, knowing that your code follows community guidelines. Search the settings for `calva-fmt` to see how you can tweak it.

## You might not need to install it

*Calva Formatter* comes bundled with [Calva](https://marketplace.visualstudio.com/items?itemName=cospaia.clojure4vscode)

## Written in ClojureScipt

Built with [Shadow CLJS](http://shadow-cljs.org/).

## By the Calva team

That's currently just me, Peter Strömberg. I'd be happy to get some more people on board, committed to make the Clojure experience in VS Code nice and pruductive.

## Something is not working?

File issues or send pull requests. You can also find me in the #editors and #calva-dev channels of Clojurains Slack.


## Parinfer and auto-adjust cursor on new lines

Calva Format and Parinfer are mostly friends, but they can conflict when this extension tries to adjust the cursor position when a new line is entered. The good news is that if you are using Parinfer you probably don't need this feature of Calva Format, and can disable it in your user settings:

```json
    "calva.fmt.autoAdjustIndentOnNewLines": false
```

## Calva Paredit recommended

It might seem a bit crazy to default to using `tab` for getting the current form indented, and maybe it is and you will change the shortcut to something else. But I recommend sticking with it a while and install [Calva Paredit](https://marketplace.visualstudio.com/items?itemName=cospaia.paredit-revived) to get great structural editing support (I am especially fond of the *slurp* and *barf* commands). Together these two extensions will make you forget about how you might have used the `tab` key before.

## How to contribute

Calva Formater is written in TypeScript and ClojureScript. It is setup so that the formatting ”decisions” are made by a library written in ClojureScript and then TypeScript is used to integrate these decisions into VS Code. Division of labour.

See [How to Contribute](https://github.com/BetterThanTomorrow/calva-fmt/wiki/How-to-Contribute) on the project wiki for instructions.

## The Future of calva-fmt
We'll see what kind of feedback people give us before deciding where we will take this extension.

## Happy Formatting

PRs welcome, file an issue or chat @pez up in the [`#calva-dev` channel](https://clojurians.slack.com/messages/calva-dev/) of the Clojurians Slack. Tweeting [@pappapez](https://twitter.com/pappapez) works too.

[![#calva-dev in Clojurians Slack](https://img.shields.io/badge/clojurians-calva--dev-blue.svg?logo=slack)](https://clojurians.slack.com/messages/calva-dev/)

❤️
