# calva-fmt

This is the [Calva Formatter](https://marketplace.visualstudio.com/items?itemName=pedrorgirardi.calva-fmt) - a Clojure and ClojureScript formatter for Visual Studio Code.

## Raison d´être

1. **Fewer dependencies, less headaches**. You should be able to edit a Clojure file, with full formatting help, without depending on a REPL running or anything else needed to be installed.
1. **Fewer conflicts, more predictability**. As VSCode gets to be a more serious editor for Clojurians there is a an editing war going on between the various plugins that help with editing Clojure code. calva-fmt is aiming at being the major Clojure formatter, lifting this responsibility from the shoulders of extensions like Calva, Paredit and Parinfer.

## Features

* Formats according to the community [Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide) plus giving you some options to tweak this style.
* Is the formater used for the VSCode *Format Selection* and *Format Document* commands.
* Is intended to be used alongside and by other Clojure extensions.

## How to use

Install it and edit away. Configure it to format the code intentionally or automatically on save, knowing that your code follows community guidelines. Search the settings for `calva-fmt` to see how you can tweak it.

## You might not need to install it

*calva-fmt* comes bundled with both [Calva](https://marketplace.visualstudio.com/items?itemName=cospaia.clojure4vscode)

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

## The Future of calva-fmt
We'll see what kind of feedback people give us before deciding where we will take this extension. But at least this is currently on our mind:
1. We want to support those who think that formatting of code should happen as you type, that there should seldom be a reason for the coder to be intentionally formatting it.