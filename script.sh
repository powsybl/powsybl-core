find . -type f \
  -not -name '*SerDe*' \
  -not -name '*Serializer*' \
  -not -name '*Deserializer*' \
  -not -name '*xml*' \
  -not -path '*/resources/*' \
  -exec grep -l -e 'boundaryLine' -e 'BoundaryLine' -e 'BOUNDARY_LINE' {} + \
  | tee /tmp/matches.txt

echo "Nombre de fichiers trouvés : $(wc -l < /tmp/matches.txt)"

find . -type f \
  -not -name '*SerDe*' \
  -not -name '*Serializer*' \
  -not -name '*Deserializer*' \
  -not -name '*xml*' \
  -not -path '*/resources/*' \
  -not -path '*/json/*' \
  -exec sed -i \
  -e 's/boundaryLine/boundaryLine/g; s/BoundaryLine/BoundaryLine/g; s/BOUNDARY_LINE/BOUNDARY_LINE/g' {} +

echo "done"


