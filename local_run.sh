# This is designed to work on my local mac, not really the department machines

classfilefolder="compiled"
jarpath="/Applications/CPLEX_Studio2211/cplex/lib/cplex.jar"
nativecode="/Applications/CPLEX_Studio2211/opl/bin/x86-64_osx"

#Can't use DYLD_LIBRARY_PATH because of Appple SIP (I think?); will use -Djava.library.path instead
#https://developer.apple.com/forums/thread/703757
java -cp $classfilefolder solver.ls.Main $1
