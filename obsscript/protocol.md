
# Layout
- ``<instruction>:<...args>``
- arguments separated by `;`

##  Requests
- Requests are writted to the ``obstate`` file
**Set current scene**
```
SetActiveScene:<name>
```

**Set browser source url**
```
SetBrowserSourceURL:<source-name>;<url>
```
**Generate player sources**

Generates *count* ammount of browser and text source pairs named p{i}-ttv and p{i}-label
```
GenPlayerSources:<scene name>;<count>
```
**Change player source to diff streamer**
```
EditPlayerSource:<scene name>;<source number>;<new ttv name>
```
Gets label and broser sources starting with p{source number} and change their properties based on the "new ttv name"


## Responses
- Responses are read from the ``obstate.out`` file

**Get all scenes**
```
GetAllScenes
```

Returns:

```
<scene1>;<scene2>;<scene3>
```