local obs = obslua


function resolve_path(base, relative)
    local separator = package.config:sub(1, 1)
    if base:sub(-1) ~= separator then
        base = base .. separator
    end
    return base .. relative
end

function getJsonPath()
    local home = os.getenv("HOME")  -- (Linux, macOS)
    if not home then
        home = os.getenv("USERPROFILE")  -- Windows
    end

    return resolve_path(resolve_path(home, ".tourneymaster"), "obsstate")
end

function readFile(path)
    local file = io.open(path, 'r')

    if not file then
        obs.script_log(obs.LOG_ERROR, "Failed to open file: " .. path)
        return nil
    end

    local content = file:read("*a") -- read the entire file
    file:close()
    return content
end

function tick()
    obs.script_log(obs.LOG_INFO, "tick")
end

-- obs event functions

function script_description ()
    return [[
        <h2>Tourney Master OBS Script</h2>

        <p>
        An interface between OBS and Tourney-Master
    ]]
end


function script_load(settings)
    obs.script_log(obs.LOG_INFO, "Tourney Master loaded")

    obs.timer_add(tick, 1000)

end

function script_unload()
    obs.timer_remove(tick)
end