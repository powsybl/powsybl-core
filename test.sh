BRANCH=$(git branch | grep "*" | awk '{print $2}')
OUT=$(git cherry -v master $BRANCH )

if [ -z "$1" ]
then
  echo "$OUT"
else
  echo "$OUT" | awk '{print $2}'
fi
