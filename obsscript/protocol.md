
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

Generates *count* ammount of groups named in the format p{i}, 
each group containing a browser source (p{i}-ttv) and a label (p{i}-label)
```
GenPlayerSources:<scene name>;<count>
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