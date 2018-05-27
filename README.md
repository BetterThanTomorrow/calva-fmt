# calva-fmt

This is the [Calva Formatter](https://marketplace.visualstudio.com/items?itemName=pedrorgirardi.calva-fmt) - a Clojure and ClojureScript formatter for Visual Studio Code.

## Raison d´être

The reasons are three-fold:

1. As VSCode gets to be a more serious editor for Clojurians there is a an editing war going on between the various plugins that help with editing Clojure code. calva-fmt is aiming at being the major Clojure formatter, lifting this responsibility from the shoulders of extensions like Calva, Paredit and Parinfer.
1. Formatting of code should happen as you type. There should seldom be a reason for the coder to be formatting it (automatically or manually). This calls for speed in the automaic formatting.
1. There should be no dependencies that the user needs to set up. You should be able to edit a Clojure file, with full formatting help, without depending on a REPL running or anything else needed to be installed.

## Features

* Formats according to the community [Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide) plus giving you some options to tweak this style.
* Formats your code as you type.
* Is the formater used for the VSCode *Format Selection* and *Format Document* commands.
* Is intended to be used alongside and by other Clojure extensions.

## How to use

Install it and edit away, knowing that your code follows community guidelines. Search the settings for `calva-fmt` to see how you can tweak it.

## You probably don't need to install it

*calva-fmt* comes bundled with both [Calva](https://marketplace.visualstudio.com/items?itemName=cospaia.clojure4vscode) and [Calva Paredit](https://marketplace.visualstudio.com/items?itemName=cospaia.paredit-revived).

## Written in ClojureScipt

Built with [Shadow CLJS](http://shadow-cljs.org/).

## By the Calva team

* Pedro Girardi 
* Peter Strömberg
* You?

## Something is not working?

File issues or send pull requests. You can also find us in the #editors and #vscode channels of Clojurains Slack.

## How to contribute

It follows the same workflow as [Calva development](https://github.com/BetterThanTomorrow/calva/wiki/How-to-Contribute)