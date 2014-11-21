mkdir -p trip-result
for SIMI in 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9
do
    for RANG in 1 2 3 4 5
    do
        time make trip-experiment threshold=${SIMI} range=${RANG} > trip-result/${SIMI}-${RANG}-trip.txt
    done
done
