# -*- coding: utf-8 -*-
# Author: matthewliu
# Created: 2019/7/29
###############################################################################

import requests
import xlwt

url = "https://api.github.com/repos/WeBankFinTech/WeEvent/issues"

# get data from github api
response = requests.get(url)
origin_data = response.json()
print("issues number: " + str(len(origin_data)))
if not origin_data:
    exit(1)

# extract need column
columns = ["number", "html_url", "title", "body", "state", "comments", "created_at", "updated_at", "closed_at"]
data = []
for issue in origin_data:
    line = {}
    for k, v in issue.items():
        if k in columns:
            line[k] = v
        elif k == "user":
            if v and "login" in v:
                line[k] = v["login"]
            else:
                line[k] = ""
        elif k == "assignees":
            assignee_str = ""
            for assignee in v:
                if assignee and "login" in assignee:
                    assignee_str = assignee["login"] + ","
            line[k] = assignee_str
        elif k == "milestone":
            if v and "title" in v:
                line[k] = v["title"]
            else:
                line[k] = ""
        elif k == "labels":
            label_str = ""
            for label in v:
                if v and "name" in label:
                    label_str = label["name"] + ","
            line[k] = label_str
    data.append(line)

# prepare excel format, header and data list
issues = [list(data[0].keys())]
for issue in data:
    issues.append(list(issue.values()))

# write into excel
book = xlwt.Workbook()
sheet = book.add_sheet("sheet1")
row = 0
for line in issues:
    col = 0
    for x in line:
        sheet.write(row, col, str(x))
        col += 1
    row += 1
book.save("./WeEvent.xls")
