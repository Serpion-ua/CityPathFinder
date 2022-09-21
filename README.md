# Run
Program could be run by using next command line arguments:

[options] <file-path> <start-node> <end-node>

Where options are

* _-fast-path_ : Return first found path, do not use that option if want to find best possible path. Disabled by default

Arguments

* _file-path_ : Path to the JSON file with traffic measurements
* _start-node_ : Start node in form AvenueName:StreetName
* _end-node_ : End node in form AvenueName:StreetName

# Limitation
All transitions between cities shall be positive
