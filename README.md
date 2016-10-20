IDEA plugin for AI.codes
===========================


Introduction
------------

This plugin brings AI-based predictive coding to IDEA IDEs. It is in early alpha now. You are welcome to download the latest build from [JetBrains](https://plugins.jetbrains.com/plugin/9203), or build from source.

It provides two predictive coding features:

* AI can understand and translate your intention to code snippet when you mark you intention as a comment line that starts with `///`.
* AI can understand your coding context and dynamically adjust the rank of code completion candidates. It saves you from scrolling up and down in the list, or type more characters just to get the right candidate.


Installation
------------

- Using IDE built-in plugin system:
  - <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Browse repositories...</kbd> > <kbd>Search for "aicodes"</kbd> > <kbd>Install Plugin</kbd>
- Manually:
  - Download the [latest release][latest-release] and install it manually using <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Install plugin from disk...</kbd>
- Note:
  - You need to also download [AI.codes dashboard](https://github.com/aicodes/dashboard) for the plugin to work. It serves as a local proxy to provide better user experience. Without the dashboard the plugin WOULD NOT work.



  Usage
  -----

  1. Most of the time AI sits in the background. Code as you normally do. The dashboard may show various probabilities that AI calculates. They are for your entertainment. Feel free to minimize the dashboard window.

  2. When you need AI to write snippets for you, express your intention in a `///` line, such as:
  `/// convert myString to int.`. Once you press <kbd>Enter</kbd>, on your next line, AI will give you
  `Integer.parseInt(myString);`. AI also takes care of imports (if any) and variable name substitution (in this case `myString`).

  3. The WebSocket connection between your IDE and the dashboard may get interrupted when you put your computer to sleep. If that happens, you can try to re-establish the connection using <kbd>Tools</kbd> > <kbd>Reconect to AI.codes Server</kbd>.

  Privacy
  -------

  1. AI-based code completion is done by sending the token's type (class name) to server. You can use regex to disable certain class name patterns by go to the Preferences setting in dashboard (not IntelliJ).

  2. AI-based snippet writing is done by sending the `/// intention line` as a query to server, together with entities mentioned in that line (if any).

  3. We **DO NOT** upload any of your source code to server. Feel free to inspect the source code of this plugin and the dashboard.


License
-------

Copyright (c) 2016 AI.codes. MIT License.
