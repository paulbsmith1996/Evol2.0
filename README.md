# Evol: A Darwinian Simulator Applet

__Authors__: Paul Baird-Smith and Ross Flieger-Allison

To run the applet from the coommand line, compile with the command

    javac Evol.java

and then execute the program with the line

    appletviewer Evol.java

No packages are required for installation, and the applet depends on existing
and readily available Java libraries. Once in the applet, a number of components
can be adjusted and the applet responds to keystrokes.

On the right of the applet is a Slider component that can be adjusted between 
values of 1 and 100. This represents the number of moves made by the creatures 
between frames. This means that for the default value of 1, each creature makes 
one move per frame, at a rate of 30 fps.

On the bottom of the applet's GUI is console, which cannot be written but is printed
in on user input. The information printed is not readable at the moment, but can be
copied into a CSV file and used to create graphs on the input.

At the bottom right corner of the applet are several interesting statistics, including
the number of prey creatures that are currently alive, the number of ancestors of the 
most developed prey creature, and the number of divisions that have occured.

The user can perform key strokes to interact with the applet. Hitting the SPACE key
will pause the environment of the applet (the creatures will stop moving). Hitting the
P key asks the applet to print some relevant information to the console. Hitting the
W key will in the future write this information to a new file.