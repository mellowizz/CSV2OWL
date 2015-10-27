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
import os
#from os.path import expanduser
'''master version '''
response = requests.get('https://docs.google.com/feeds/download/spreadsheets/Export?key=1DetLdQWehIFy31pFduqU20o_EO8KziKxWiORkhXzzQc&exportFormat=csv&gid=403134972', stream=True)
''' simons version'''
#reponse = requests.get('https://docs.google.com/feeds/download/spreadsheets/Export?key=1oa4IThXlJa-1VXXtQs5AApHl0qEyPZd-p5RPiy6P9vk&exportFormat=csv&gid=1755891071', stream=True)
response.raise_for_status()

#home = os.path.expanduser("~")

#output_file = os.path.join(home, "test-rlp", "rlp_key_latest.csv")
#output_file = output_dir + rlp_key_latest.csv'
output_file = sys.argv[1]
print(output_file)
if sys.version < '3':
    infile = io.open(output_file, 'wb')
else:
    infile = io.open(output_file, 'wb')
#print(response.content)
with infile as csv:
    response.raw.decode_content = True
    shutil.copyfileobj(response.raw, csv)