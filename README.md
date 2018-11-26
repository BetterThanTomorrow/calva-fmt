# calva-fmt

This is the [Calva Formatter](https://marketplace.visualstudio.com/items?itemName=cospaia.calva-fmt) - a Clojure and ClojureScript formatter for Visual Studio Code.

## Raison d´être

1. **Fewer dependencies, less headaches**. You should be able to edit a Clojure file, with full formatting help, without depending on a REPL running or anything else needed to be installed.
1. **Fewer conflicts, more predictability**. As VSCode gets to be a more serious editor for Clojurians there is a an editing war going on between the various plugins that help with editing Clojure code. Calva Formatter is aiming at being the major Clojure formatter, lifting this responsibility from the shoulders of extensions like Calva, Paredit and other Clojure related extensions.

## Features

* Formats according to the community [Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide) plus giving you some options to tweak this style.
* Adds command for formatting the enclosing form, default key binding is `tab`.
* Adds command for aligning map items, and bindings in the current form, default key binding `ctrl+alt+l`. (This is a bit experimental and will not always produce the prettiest results. Also it is recursive.) You can also opt-in to have this behaviour be on for all formatting, via settings.
* Adds command for infering parens/brackets from indents (using ParinferLib), default key binding `ctrl+alt+p`.
* Adds command for indenting and dedenting the current line (using ParinferLib), default key binding `ctrl+i` and `shift+ctrl+i`, respectively.
* Provides the formater for the VSCode *Format Selection* and *Format Document* commands.
* Is intended to be used alongside and by other Clojure extensions. **(Though it conflicts with Parinfer, see below.)**
* Formats the code when new lines are entered, mostly keeping things formated as you type.

_Format Current Form_

![Format Current Form](/assets/format-current-form.gif)

_Parinfer_

![Format Current Form](/assets/parinfer.gif)

_Align Current Form_

![Align Current Form](/assets/align-items.gif)

## How to use

Install it and edit away. It will keep the code fomatted mostly as you type, in a somewhat ”relaxed” way, and will format it more strictly (collecting trailing brackets, for instance) when you hit `tab tab`. Search the settings for `calva-fmt` to see how you can tweak it.


## You might not need to install it

*Calva Formatter* comes bundled with [Calva](https://marketplace.visualstudio.com/items?itemName=cospaia.clojure4vscode)

## Written in ClojureScipt

Built with [Shadow CLJS](http://shadow-cljs.org/).

## By the Calva team

That's currently just me, Peter Strömberg. I'd be happy to get some more people on board, committed to make the Clojure experience in VS Code nice and pruductive.

## Something is not working?

File issues or send pull requests. You can also find me in the #editors and #calva-dev channels of Clojurains Slack.


## Disable the Parinfer Extension

Calva Formatter and the current Parinfer extension are not compatible. Some Parinfer functionality is is built in, though, in the form of explicit commands, see above feature list.

## Calva Paredit recommended

It might seem a bit crazy to default to using `tab` for getting the current form indented, and maybe it is and you will change the shortcut to something else. But I recommend sticking with it a while and install [Calva Paredit](https://marketplace.visualstudio.com/items?itemName=cospaia.paredit-revived) to get great structural editing support (I am especially fond of the *slurp* and *barf* commands). Together these two extensions will make you forget about how you might have used the `tab` key before.

## How to contribute

Calva Formater is written in TypeScript and ClojureScript. It is setup so that the formatting ”decisions” are made by a library written in ClojureScript and then TypeScript is used to integrate these decisions into VS Code. Division of labour.

See [How to Contribute](https://github.com/BetterThanTomorrow/calva-fmt/wiki/How-to-Contribute) on the project wiki for instructions.

## The Future of calva-fmt
* Make it honor project settings.
* Up the Parinfer magic support.


## Happy Formatting

PRs welcome, file an issue or chat @pez up in the [`#calva-dev` channel](https://clojurians.slack.com/messages/calva-dev/) of the Clojurians Slack. Tweeting [@pappapez](https://twitter.com/pappapez) works too.

[![#calva-dev in Clojurians Slack](https://img.shields.io/badge/clojurians-calva--dev-blue.svg?logo=slack)](https://clojurians.slack.com/messages/calva-dev/)

❤️
