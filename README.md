GA-Paint
========

The GA-Paint program is simple evolutionary algorithm to convert any picture
(i.e jpg, png, etc.) into a series of semi-transparent, overlapping polygons.

At the moment the project is more of a proof-of-concept-trial-and-error-
how-does-github-work-in-progress-with-no-testcases type of monstrosity then
anything else. 

The graphical output can then be saved into a Json object and used within 
other applications.

Usage
---------

Run go.sh to start the process:

For example:

    ./go.sh bill-in-skipants.jpg 300

To output the result as a JSon object

    ./dump eastate bill-in-skipatns.json

To clear everything and start affresh:

    ./prep.sh
   
