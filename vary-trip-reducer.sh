mkdir -p trip-reducer-result
for num in 1 2 3 4 5 6 7 8 9 10
do
    time make trip-vary-reducer reducer=$num > trip-reducer-result/$num.txt 2>&1
done
