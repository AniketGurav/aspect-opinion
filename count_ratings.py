#!/usr/bin/python

#
# Plot rating distributions of a dataset
# Author: Pengfei Liu (pfliu@se.cuhk.edu.hk)
# Copyright: The Chinese University of Hong Kong
#

from __future__ import division

import sys
import numpy as np

from collections import Counter

def get_counters(data_file, rate_num):
    """
    Count ratings for each data file
    """
    counters = []
    for idx in range(rate_num):
        counter = Counter({'1':0, '2':0, '3':0, '4':0, '5':0})
        counters.append(counter)

    START_IDX = 2
    with open(data_file, 'r') as rate_file:
        for line in rate_file:
            ratings = line.strip().split('\t')
            if len(ratings) != (START_IDX + rate_num):
                continue
            for idx in range(rate_num):
                counters[idx][str(ratings[START_IDX+idx])] += 1

    result = []
    for cnt in counters:
        numbers = [cnt['1'], cnt['2'], cnt['3'], cnt['4'], cnt['5']]
        total = sum(numbers)
        if total == 0:
            total = 1
        result.append([round(num*100/total,2) for num in numbers])

    return np.array(result).transpose()

if __name__ == '__main__':
    """
    Read a list of data files and plot a comparison graph
    """
    file_path = sys.argv[1]
    asp_num = int(sys.argv[2])

    counters = get_counters(file_path, asp_num)
    print counters
