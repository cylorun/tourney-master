local obs = obslua

-- constants
REFRESH_RATE_MS = 1000

-- util functions

function resolve_path(base, relative)
    local separator = package.config:sub(1, 1)
    if base:sub(-1) ~= separator then
        base = base .. separator
    end

    return base .. relative
end

function get_obsstate_path()
    local home = os.getenv("HOME")  -- (Linux, macOS)
    if not home then
        home = os.getenv("USERPROFILE")  -- Windows
    end

    return resolve_path(resolve_path(home, ".tourneymaster"), "obsstate")
end

function read_file(path)
    local file = io.open(path, 'r')

    if not file then
        obs.script_log(obs.LOG_ERROR, "Failed to open file: " .. path)
        return nil
    end

    local content = file:read("*a") -- read the entire file
    file:close()
    return content
end

local function split(str, delimiter)
    local returnTable = {}
    for k, v in string.gmatch(str, "([^" .. delimiter .. "]+)") do
        returnTable[#returnTable+1] = k
    end
    return returnTable
end

function get_obsstate()
    return read_file(get_obsstate_path())
end



-- obs functions

function get_scene(name)
    local source = get_source(name)
    if source == nil then return nil end

    local scene = obs.obs_scene_from_source(source)
    release_source(source)
    return scene
end

function get_source(source_name)
    return obs.obs_get_source_by_name(source_name)
end

function release_source(source)
    obs.obs_source_release(source)
end

function release_scene(scene)
    obs.obs_scene_release(scene)
end

function scene_exists(name)
    return get_scene(name) ~= nil
end

function create_scene(name)
    release_scene(obs.obs_scene_create(name))
end

function set_scene(name)
    local scene = get_source(name)

    if scene ~= nil then
        obs.obs_frontend_set_current_scene(scene)
        release_source(scene)
        obs.script_log(obs.LOG_INFO, "Switched to scene: " .. name)
    else
        obs.script_log(obs.LOG_ERROR, "Scene " .. name .. " does not exist")
    end
end

function tick()
    local obsstate = get_obsstate()
    if not obsstate then
        obs.script_log(obs.LOG_ERROR, "obsstate file not found")
        return
    end

    local instruction, args = string.match(obsstate, "([^:]+):(.+)")
    args = split(args, ';');

    if #args == 0 then
        obs.script_log(obs.LOG_ERROR, "Too few arguments for " .. instruction)
        return
    end
    if instruction == "SS" then
        local scene = args[1]
        set_scene(scene)
    end

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

    obs.timer_add(tick, REFRESH_RATE_MS)
end

function script_unload()
    obs.timer_remove(tick)
end