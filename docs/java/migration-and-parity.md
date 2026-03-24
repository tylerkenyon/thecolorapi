# Migration and 1:1 Parity Notes

## Scope of parity completed

The Java implementation now provides local equivalents for all functional capabilities previously delivered through the JS route layer:

- identify from hex/rgb/hsl/hsv/cmyk
- parse query-style values
- parse unknown-style values
- color naming and distance calculation
- contrast recommendation
- scheme generation for all legacy modes
- random color generation
- color and scheme SVG generation

## Key parity behaviors

- `parseQueryColors` precedence mirrors legacy selection order.
- `parseUnknownType` detection mirrors legacy string-prefix and hex heuristics.
- scheme ratio distribution follows weighted count assignment.
- output map keys and nesting align with legacy payload style.

## Deliberate compatibility constraints

- `ColorApiResponse` is retained as compatibility surface for old wrapper integrations.
- compatibility URL strings in `image` and `_links` are preserved in payload output shape.

## Advanced mode

`advanced` is additive and does not replace legacy modes.

It is designed for richer palette variety while preserving deterministic generation mechanics from a seed.

## Removing JS runtime

With Java local parity available, legacy JS runtime files can be removed from active implementation paths.

This repository update removes route and lib JS implementation files and updates root documentation to make Java the primary maintained implementation.
