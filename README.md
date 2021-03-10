# Associative-Memory

The algorithm takes in any number of images (50x50 pixels), builds a weight matrix and uses it to find the original stored image given a broken one.
The program can be run directly from cmd with `java AssociativeMemory`, you are then prompted to give the path of the broken image, which will be displayed alongside the resulting match (direct or specular).
Further information about the results are given in the terminal.
Note that because of high correlation between images the maximum number of storable images is approximately 0.005\*n where n is the number of pixels.
With 2500 pixels up to 12 images can be stored.
