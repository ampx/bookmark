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

## Query Transaction Bookmark

Get stored progress for a specific bookmark

**URL** : `/bookmarks/{bookmark_name}/txn/{transaction_context}`
**Method** : `GET`

### URL Parameters

**Default** - Will return last transaction inserted

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
TRANSACTION_CONTEXT=transaction0
URL_PARAM="?from=`date -d '1 day ago' '+%FT%T.000Z'`"

curl http://$URL/bookmarks/$BOOKMARK_NAME/txn/$TRANSACTION_CONTEXT$URL_PARAM
```

### Notes

------------------------------

## Update Transaction Bookmark

Append a bookmark with new progress entries

**URL** : `/bookmarks/{bookmark_name}/txn/{transaction_context}`
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
TRANSACTION_CONTEXT=transaction0
BOOKMARK_DATA="[{\"timestamp\":{\"time\":\"`date '+%FT%T.000Z'`\"},\"metrics\":null}]"

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME/txn/$TRANSACTION_CONTEXT
```

### Notes

------------------------------

## Save Transaction Bookmark

Overwrite all existing bookmark's progress records with new ones

**URL** : `/bookmarks/{bookmark_name}/txn/{transaction_context}`
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
TRANSACTION_CONTEXT=transaction0
BOOKMARK_DATA="[{\"timestamp\":{\"time\":\"`date '+%FT%T.000Z'`\"},\"metrics\":null}]"

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X POST http://$URL/bookmarks/$BOOKMARK_NAME/txn/$TRANSACTION_CONTEXT
```

### Notes

---------------------------

## Query Bookmark State

Get state of the bookmark 

**URL** : `/bookmarks/{bookmark_name}/state/`
**Method** : `GET`

### URL Parameters

**Default** - Will return all key value pairs

**Optional:**
* ```?data=state_name``` - name of individual state value

### Data Parameters

N/A

### Success Response

```json
{"state_value0":"value0", "state_value1":"value1",...}
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

Updates provided state value

**URL** : `/bookmarks/{bookmark_name}/state`
**Method** : `PUT`

### URL Parameters

None

### Data Parameters

```json
{"state_value0":"value0"}
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
BOOKMARK_DATA='{"state_value0":"value0"}'

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X PUT http://$URL/bookmarks/$BOOKMARK_NAME/state
```

### Notes

------------------------------

## Save Bookmark State

Overwrite all existing state values with new ones

**URL** : `/bookmarks/{bookmark_name}/state`
**Method** : `POST`

### URL Parameters

None

### Data Parameters

```json
{"state_value0":"value0"}
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
BOOKMARK_DATA='{"state_value0":"value0"}'

curl -d "$BOOKMARK_DATA" -H "Content-Type: application/json" -X POST http://$URL/bookmarks/$BOOKMARK_NAME/state
```

### Notes

------------------------------
