# -*- coding: utf-8 -*-
# Author: matthewliu
# Created: 2019/7/29
###############################################################################

import requests
import sys
import time
import xlwt


def load_all_issues(milestone):
    '''
    load all issues in milestone, except PR
    :param milestone: milestone in project
    :return: True if success, list of issues
    '''

    # page is 1-based, 30 items by default
    url = "https://api.github.com/repos/WeBankFinTech/WeEvent/issues?state=all&milestone=%s&per_page=100" % milestone
    page = 1
    origin_data = []
    while True:
        url_once = "%s&page=%s" % (url, page)
        print("fetch url: %s" % url_once)
        response = requests.get(url_once, timeout=5)
        if response.status_code != 200:
            print("github api status code is %s" % response.status_code)
            return False, origin_data
        data_once = response.json()
        if data_once:
            # skip PR, PR has value in key="pull_request"
            origin_data.extend([x for x in data_once if not ("pull_request" in x and x["pull_request"])])
            page += 1
            # sleep to avoid anti-spider
            time.sleep(1)
        else:
            break

    print("total issues number: %s" % len(origin_data))
    return True, origin_data


def export_issues(milestone, output):
    '''
    get all issues in milestone like "v1.1.0"
    see https://developer.github.com/v3/
    :param milestone: milestone in project
    :param output: excel file name
    :return: True if success
    '''

    # get data from github api
    result, origin_data = load_all_issues(milestone)
    if not result:
        return False

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
                        assignee_str += assignee["login"] + ","
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
                        label_str += label["name"] + ","
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

    print("save issues into excel: %s" % output)
    book.save(output)
    return True


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage:\n\t%s 2\n\t%s v1.1.0" % (sys.argv[0], sys.argv[0]))
        exit(1)

    milestone = sys.argv[1]
    print("try to load issues in milestone: %s" % milestone)
    export_issues(milestone, "WeEvent.xls")
