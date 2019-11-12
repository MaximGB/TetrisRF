# Tetris written with ClojureScript/re-frame

The project is my atempt to get acquainted with re-frame library which resulted in two libraries birth:

- maximgb.re-service - a universal way to translate re-frame effect requests into function calls
- maximgb.re-state   - a component isolation and re-frame event handling with Hoare statecharts currently backed
                       by JavaScript [XState](https://xstate.js.org) library.

(at the moment of writing the libraries are not yet moved into a separate repositories, but that's a matter of days)

## Demo

The resulting and playable demo can be found [here](https://maximgb.github.io/TetrisRF/example/).
<div style="text-align: center">
  <a href="https://maximgb.github.io/TetrisRF/example" title="Go to the demo">
    <img src="https://maximgb.github.io/TetrisRF/images/demo.png" alt="Demo screenshot"/>
  </a>
</div>


## Setup instructions

```shell
git clone git@github.com:MaximGB/TetrisRF.git
cd ./TetrisRF
make npmi
make run
```
