#!/bin/bash

for f in *.plot; do
    gnuplot "$f";
done

cp *.pdf ../report/figures/plots

