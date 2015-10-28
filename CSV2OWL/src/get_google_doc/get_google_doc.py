#!/usr/bin/python2
# -*- coding: utf-8 -*-
"""
Created on Wed Sep 16 16:24:22 2015

@author: Moran
"""
import requests
import shutil
import sys
import io

GOOGLE = 'https://docs.google.com/feeds/download/spreadsheets/Export?'
FORMAT = 'csv'

# s = "path: %(path)s curr: %(curr)s prev: %(prev)s" % data

'''master version '''
my_doc = {'key': '1DetLdQWehIFy31pFduqU20o_EO8KziKxWiORkhXzzQc',
          'gid': '403134972', 'format': FORMAT, 'google': GOOGLE}

''' simons version'''
simon_doc = {'key': '1oa4IThXlJa-1VXXtQs5AApHl0qEyPZd-p5RPiy6P9vk',
             'gid': '1755891071', 'format': FORMAT, 'google': GOOGLE}


full_url = '{google}key={key}&exportFormat={format}&gid={gid}'.format(**my_doc)

response = requests.get(full_url, stream=True)
response.raise_for_status()

output_file = sys.argv[1]
print(output_file)
if sys.version < '3':
    infile = io.open(output_file, 'wb')
else:
    infile = io.open(output_file, 'wb')

# print(response.content)
''' write google doc to file '''
with infile as csv:
    response.raw.decode_content = True
    shutil.copyfileobj(response.raw, csv)
