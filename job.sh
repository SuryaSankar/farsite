host=$1
farsiteHome=~/farsite
shift
cmd="(cd $farsiteHome ;$@)";
ssh  $host  $cmd  2>&1 | sed "s/^/$host: /";	#This does everything
echo
