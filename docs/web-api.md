------------------------------

## Service Check

Method to check if server is up

**URL** : `/bookmarks`
**Method** : `HEAD`

### URL Parameters

N/A

### Data Parameters

N/A

### Success Response

If Service is up:

**Code:** `200`

### Error Response

### Sample

```shell
URL=localhost:8080

curl -I http://$URL/bookmarks
```

### Notes

------------------------------

## Get Bookmark Service Data

Get bookmark service information. At the moment only list of available bookmarks is available

**URL** : `/bookmarks`
**Method** : `GET`

### URL Parameters

**Default** - Will return Code `200`

**Optional:**
* ```?data=bookmarks``` - return list of all bookmarks

### Data Parameters

N/A

### Success Response

Request for a list of available bookmarks will return

```json
[
  {"bookmarks":["bookmark0", "bookmark1"]}
]
```

### Error Response

### Sample

```shell
URL=localhost:8080
URL_PARAM="?data=bookmarks"

curl http://$URL/bookmarks$URL_PARAM
```

### Notes

------------------------------

## Register New Bookmark

Register a new bookmark on the server

**URL** : `/bookmarks`
**Method** : `PUT`

### URL Parameters

N/A

### Data Parameters

**Required Data Values**:

Name of the new bookmark

```json
{"name": "bookmark0"}
```

**Optional Configurations:**

To set a costume retention policy for the new bookmark:

```json
{"config":{"retentionDays":30}}
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors will result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA='{"name": "bookmark0", "config":{"retentionDays":30}}'

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/
```

### Notes








------------------------------

## Bookmark Exists

Method to check if bookmark exists

**URL** : `/bookmarks/{bookmark_name}`
**Method** : `HEAD`

### URL Parameters

N/A

### Data Parameters

N/A

### Success Response

If bookmark exists:

**Code:** `200`

### Error Response

If bookmark doesn't exist:

**Code:** `400`

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0

curl -I http://$URL/bookmarks/$BOOKMARK_NAME
```

### Notes

------------------------------

## Update Bookmark Configurations

Update Bookmark configurations. This method will only update configurations elements provided.

**URL** : `/bookmarks/{bookmark_name}`
**Method** : `PUT`

### URL Parameters

None

### Data Parameters

```json
{"config":{"retentionDays":31}}
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors with result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA='{"config":{"retentionDays":31}}'

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME
```

### Notes

------------------------------

## Overwrite Bookmark Configurations

This method will delete all existing bookmark configurations and set provided configurations

**URL** : `/bookmarks/{bookmark_name}`
**Method** : `POST`

### URL Parameters

None

### Data Parameters

```json
{"config":{"retentionDays":31}}
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors will result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA='{"config":{"retentionDays":31}}'

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X POST http://$URL/bookmarks/$BOOKMARK_NAME
```

### Notes










------------------------------

## Query Bookmark Progress

Get stored progress for a specific bookmark

**URL** : `/bookmarks/{bookmark_name}/bookmark`
**Method** : `GET`

### URL Parameters

**Default** - Will return last bookmark

**Optional:**
* ```?data=*``` - return all bookmarks
* ```?top=10``` - most recent 10
* ```?top=-10``` - oldest 10
* ```from=2018-12-10T13:45:00Z```
* ```?to=2018-12-11T13:45:00Z```

### Data Parameters
N/A

### Success Response

```json
[
  {"timestamp":{"time":"2018-12-10T13:45:00Z"},"metrics":{"metric0":"metric0Value",...}}}
]
```

### Error Response

If bookmark doesn't exist

**Code:** `400`

### Sample

Get progress for the last 24 hours

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
URL_PARAM="?from=`date -d '1 day ago' '+%FT%T.000Z'`"

curl http://$URL/bookmarks/$BOOKMARK_NAME/bookmark$URL_PARAM
```

### Notes

------------------------------

## Update Bookmark Progress

Append a bookmark with new progress entries

**URL** : `/bookmarks/{bookmark_name}/bookmark`
**Method** : `PUT`

### URL Parameters

None

### Data Parameters

```json
[
  {"timestamp":{"time":"2018-12-10T13:45:00Z"},"metrics":{"metric0":"metric0Value",...}}}
]
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors with result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA="[{\"timestamp\":{\"time\":\"`date '+%FT%T.000Z'`\"},\"metrics\":null}]"

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME/bookmark
```

### Notes

------------------------------

## Save Bookmark Progress

Overwrite all existing bookmark's progress records with new ones

**URL** : `/bookmarks/{bookmark_name}/bookmark`
**Method** : `POST`

### URL Parameters

None

### Data Parameters

```json
[
  {"timestamp":{"time":"2018-12-10T13:45:00Z"},"metrics":{"metric0":"metric0Value",...}}}
]
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors with result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA="[{\"timestamp\":{\"time\":\"`date '+%FT%T.000Z'`\"},\"metrics\":null}]"

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X POST http://$URL/bookmarks/$BOOKMARK_NAME/bookmark
```

### Notes

---------------------------

## Query Bookmark State

Get state of the bookmark 

**URL** : `/bookmarks/{bookmark_name}/state`
**Method** : `GET`

### URL Parameters

None

### Data Parameters

N/A

### Success Response

```json
{"state":0}
```

### Error Response

If bookmark doesn't exist:

**Code:** `400`

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0

curl http://$URL/bookmarks/$BOOKMARK_NAME/state
```

### Notes

------------------------------

## Update Bookmark State

Update bookmark's state

**URL** : `/bookmarks/{bookmark_name}/state`
**Method** : `PUT` or `POST`

### URL Parameters

None

### Data Parameters

```json
{"state":0}
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors will result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA='{"state":1}'

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME/state
```

### Notes

Available states:
* 0 - Unlocked
* 1 - Locked
* 2 - Error

------------------------------

## Query Bookmark Failed

Get stored metrics for failed runs

**URL** : `/bookmarks/{bookmark_name}/failed`
**Method** : `GET`

### URL Parameters

**Default** - Will return last record

**Optional:**
* ```?data=*``` - return all records
* ```?top=10``` - latest 10
* ```?top=-10``` - oldest 10
* ```from=2018-12-10T13:45:00Z```
* ```?to=2018-12-11T13:45:00Z```

### Data Parameters
N/A

### Success Response

```json
[
  {"timestamp":{"time":"2018-12-10T13:45:00Z"},"metrics":{"metric0":"metric0Value",...}}}
]
```

### Error Response

If bookmark doesn't exist

**Code:** `400`

### Sample**

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
URL_PARAM="?from=`date -d '1 day ago' '+%FT%T.000Z'`"

curl http://$URL/bookmarks/$BOOKMARK_NAME/failed$URL_PARAM
```

### Notes

------------------------------

## Update Bookmark Failed

Append a bookmark with new failed entries

**URL** : `/bookmarks/{bookmark_name}/failed`
**Method** : `PUT`

### URL Parameters

None

### Data Parameters

```json
[
  {"timestamp":{"time":"2018-12-10T13:45:00Z"},"metrics":{"metric0":"metric0Value",...}}}
]
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors with result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA="[{\"timestamp\":{\"time\":\"`date '+%FT%T.000Z'`\"},\"metrics\":null}]"

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME/failed
```

### Notes

------------------------------

## Save Bookmark Failed

Overwrite all existing bookmark's failed records with new ones

**URL** : `/bookmarks/{bookmark_name}/bookmark`
**Method** : `POST`

### URL Parameters

None

### Data Parameters

```json
[
  {"timestamp":{"time":"2018-12-10T13:45:00Z"},"metrics":{"metric0":"metric0Value",...}}}
]
```

### Success Response

```json
{
  "success": true
}
```

### Error Response

Any issues with the request or bookmark service internal errors with result in:

```json
{
  "success": false
}
```

### Sample

```shell
URL=localhost:8080
BOOKMARK_NAME=bookmark0
BOOKMARK_DATA="[{\"timestamp\":{\"time\":\"`date '+%FT%T.000Z'`\"},\"metrics\":null}]"

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME/failed
```

### Notes

