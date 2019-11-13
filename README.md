# Tetris written with ClojureScript/re-frame

The project was initiated to get better acquainted with re-frame library, as the result two other libraries have been developed:

- [maximgb.re-service](https://github.com/MaximGB/re-service) - a universal way to translate re-frame co-effect/effect requests into function calls
- maximgb.re-state   - a component isolation and re-frame event handling with Hoare statecharts, currently backed
                       by JavaScript [XState](https://xstate.js.org) library.

(at the moment of writing the re-state is not yet moved into a separate repository, but that's a matter of days)

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
make run
```
