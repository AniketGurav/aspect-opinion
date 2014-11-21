mkdir -p yelp-reducer-result
for num in 1 2 3 4 5 6 7 8 9 10
do
    time make yelp-vary-reducer reducer=$num > yelp-reducer-result/$num.txt 2>&1
done
