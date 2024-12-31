
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
**Generate player sources from file**
```
GenPlayerSources:<scene name>;<ttv file path>
```


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