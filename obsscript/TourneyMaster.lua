local obs = obslua


last_obsstate = ""

-- constants
REFRESH_RATE_MS = 250

-- util functions


-- IO operations

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

function get_obsstate_out_path()
    return get_obsstate_path() .. ".out"
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

function write_file(path, data)
    local file = io.open(path, 'w')
    if not file then
        obs.script_log(obs.LOG_ERROR, "Error: Could not open file " .. path .. " for writing.")
        return
    end

    file:write(data)
    file:close()
end

function get_obsstate()
    return read_file(get_obsstate_path())
end

function write_obsstate_out(data)
    write_file(get_obsstate_out_path(), data)
end


-- String operations

function split(str, delimiter)
    local returnTable = {}
    for k, v in string.gmatch(str, "([^" .. delimiter .. "]+)") do
        returnTable[#returnTable+1] = k
    end
    return returnTable
end

function string_starts_with(str, prefix)
    return string.sub(str, 1, #prefix) == prefix
end



-- obs functions

function get_all_scene_names()
    local scenes = obs.obs_frontend_get_scenes()
    if scenes == nil then
        obs.script_log(obs.LOG_ERROR, "No scenes found.")
        return {}
    end

    local scene_names = {}
    for _, scene in ipairs(scenes) do
        local name = obs.obs_source_get_name(scene)
        table.insert(scene_names, name)
    end

    obs.source_list_release(scenes)

    return scene_names
end

function get_current_scene()
    local current_scene = obs.obs_frontend_get_current_scene()
    if current_scene ~= nil then
        local scene_name = obs.obs_source_get_name(current_scene)
        release_source(current_scene)
        return scene_name
    else
        obs.script_log(obs.LOG_ERROR, "No current scene found")
        return nil
    end
end

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
    local curr_scene_name = get_current_scene()
    if curr_scene_name == name then
        return
    end

    local scene = get_source(name)
    if scene ~= nil then
        obs.obs_frontend_set_current_scene(scene)
        release_source(scene)
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

    if last_obsstate == obsstate then
        return
    end

    last_obsstate = obsstate

    local instruction, args = string.match(obsstate, "([^:]+):?(.*)")

    if not instruction then
        obs.script_log(obs.LOG_ERROR, "Invalid obsstate format. Expected <instruction>:<args> or just <instruction>")
        return
    end

    if args then
        args = split(args, ';')
    else
        args = {}
    end

    if string_starts_with(instruction, "Get") then
        local success, message = parse_get_instr(instruction, args)
        if not success then
            obs.script_log(obs.LOG_ERROR, message)
        end
    elseif string_starts_with(instruction, "Set") then
        local success, message = parse_set_instr(instruction, args)
        if not success then
            obs.script_log(obs.LOG_ERROR, message)
        end
    else
       obs.script_log(obs.LOG_ERROR, "Unknown instuction: " .. instruction)
    end
end

function parse_get_instr(instruction, args)
    if instruction == "GetAllScenes" then
        local scenes_list = get_all_scene_names()
        local scenes_str = ""

        for _, name in ipairs(scenes_list) do
            scenes_str = scenes_str .. name .. ";"
        end

        write_obsstate_out(scenes_str)

        return true
    end
end

function parse_set_instr(instruction, args)
    if instruction == "SetActiveScene" then
        if not args or #args == 0 then
            return false, "Missing arguments"
        end

        local scene_name = args[1]
        set_scene(scene_name)
        return true
    end

    return false, "Invalid instruction: " .. instruction
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