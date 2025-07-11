# README

Ordinance survey open data of houses in Bitterne Park Southamptpon

## raw UPRN data 

File: uprnBitternePk1.csv

UPRN =  The Unique Property Reference Number (UPRN) is the unique identifier for every addressable location across the UK.

Note UPRN has leading ' to avoid interpretation as  number

csv column order: Latitude,Longitude,UPRN  

## Enhanced UPRN data

uprnBitternePk1nominatum.json  json responses from nominatum reverse geocode lookup for UPRN lat lon

uprnBitternePk1nominatum-raw.csv csv enhanced with json responses (as received)

uprnBitternePk1nominatum-modified.csv csv enhanced with json responses sorted by road and tidied up

csv column order: "Asset_latitude,Asset_longitude,UPRN,road,houseNumber,fullAddress"

