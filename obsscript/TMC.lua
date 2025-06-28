local obs = obslua


last_obsstate = ""

-- constants
REFRESH_RATE_MS = 100

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

function isWindows()
    return package.config:sub(1, 1) == "\\"
end

function get_textsource_type()
    if isWindows() then
        return "text_gdiplus"
    end

    return "text_ft2_source"
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

function create_player_group(scene_name, group_name)
    local scene_source = obs.obs_get_source_by_name(scene_name)
    if not scene_source then
        obs.script_log(obs.LOG_ERROR, "Scene not found: " .. scene_name)
        return
    end

    local scene = obs.obs_scene_from_source(scene_source)
    if not scene then
        obs.script_log(obs.LOG_ERROR, "Failed to get scene object")
        obs.obs_source_release(scene_source)
        return
    end

    local browser_settings = obs.obs_data_create()
    obs.obs_data_set_string(browser_settings, "url", "https://player.twitch.tv/?channel=cylorun&enableExtensions=false&muted=true&parent=twitch.tv&player=popout&quality=chunked&volume=0.01")

    local browser_source = obs.obs_source_create("browser_source", group_name .. "-ttv", browser_settings, nil)
    obs.obs_data_release(browser_settings)

    local text_settings = obs.obs_data_create()
    obs.obs_data_set_string(text_settings, "text", "cylorun he/him \n PB: 9:46")
    local text_source = obs.obs_source_create(get_textsource_type(), group_name .. "-label", text_settings, nil)
    obs.obs_data_release(text_settings)

    local scene_item_browser = obs.obs_scene_add(scene, browser_source)
    local scene_item_text = obs.obs_scene_add(scene, text_source)

    if scene_item_browser and scene_item_text then
        obs.script_log(obs.LOG_INFO, "Sources added to scene: " .. group_name)
    else
        obs.script_log(obs.LOG_ERROR, "Failed to add sources to scene: " .. group_name)
    end

    obs.obs_source_release(browser_source)
    obs.obs_source_release(text_source)
    obs.obs_source_release(scene_source)

    obs.script_log(obs.LOG_INFO, "Created and added browser and text sources to scene: " .. scene_name)
end

function edit_player_source(scene_name, num, new_name, player_label)
    local browser_name = "p" .. num .. "-ttv"
    local label_name = "p" .. num .. "-label"

    local current_scene = obs.obs_get_scene_by_name(scene_name)
    if not current_scene then
        obs.script_log(obs.LOG_ERROR, "Scene not found: " .. scene_name)
        return
    end

    local function get_source(scene, source_name)
        local scene_item = obs.obs_scene_find_source(scene, source_name)
        if scene_item then
            return obs.obs_sceneitem_get_source(scene_item)
        end
        return nil
    end

    local browser_source = get_source(current_scene, browser_name)
    if browser_source then
        local settings = obs.obs_source_get_settings(browser_source)
        obs.obs_data_set_string(settings, "url", "https://player.twitch.tv/?channel=" .. new_name .. "&enableExtensions=false&muted=true&parent=twitch.tv&player=popout&quality=chunked&volume=0.01")
        obs.obs_source_update(browser_source, settings)
        obs.obs_data_release(settings)
    else
        obs.script_log(obs.LOG_ERROR, "Browser source not found: " .. browser_name)
    end

    local text_source = get_source(current_scene, label_name)
    if text_source then
        local settings = obs.obs_source_get_settings(text_source)
        obs.obs_data_set_string(settings, "text", player_label)
        obs.obs_source_update(text_source, settings)
        obs.obs_data_release(settings)
    else
        obs.script_log(obs.LOG_ERROR, "Text source not found: " .. label_name)
    end

    obs.obs_scene_release(current_scene)
end

function swap_player_sources(scene_name, source1num, source2num)
    local browser_name_1 = "p" .. source1num .. "-ttv"
    local label_name_1 = "p" .. source1num .. "-label"
    local browser_name_2 = "p" .. source2num .. "-ttv"
    local label_name_2 = "p" .. source2num .. "-label"

    local current_scene = obs.obs_get_scene_by_name(scene_name)
    if not current_scene then
        obs.script_log(obs.LOG_ERROR, "Scene not found: " .. scene_name)
        return
    end

    local function get_source(scene, source_name)
        local scene_item = obs.obs_scene_find_source(scene, source_name)
        if scene_item then
            return obs.obs_sceneitem_get_source(scene_item)
        end
        return nil
    end

    local browser_source_1 = get_source(current_scene, browser_name_1)
    local label_source_1 = get_source(current_scene, label_name_1)

    local browser_source_2 = get_source(current_scene, browser_name_2)
    local label_source_2 = get_source(current_scene, label_name_2)

    if not (browser_source_1 and label_source_1 and browser_source_2 and label_source_2) then
        obs.script_log(obs.LOG_ERROR, "One or more sources not found for players " .. source1num .. " and " .. source2num)
        obs.obs_scene_release(current_scene)
        return
    end

    local browser_settings_1 = obs.obs_source_get_settings(browser_source_1)
    local browser_settings_2 = obs.obs_source_get_settings(browser_source_2)
    local label_settings_1 = obs.obs_source_get_settings(label_source_1)
    local label_settings_2 = obs.obs_source_get_settings(label_source_2)

    local url_1 = obs.obs_data_get_string(browser_settings_1, "url")
    local url_2 = obs.obs_data_get_string(browser_settings_2, "url")
    obs.obs_data_set_string(browser_settings_1, "url", url_2)
    obs.obs_data_set_string(browser_settings_2, "url", url_1)
    obs.obs_source_update(browser_source_1, browser_settings_1)
    obs.obs_source_update(browser_source_2, browser_settings_2)

    local text_1 = obs.obs_data_get_string(label_settings_1, "text")
    local text_2 = obs.obs_data_get_string(label_settings_2, "text")
    obs.obs_data_set_string(label_settings_1, "text", text_2)
    obs.obs_data_set_string(label_settings_2, "text", text_1)
    obs.obs_source_update(label_source_1, label_settings_1)
    obs.obs_source_update(label_source_2, label_settings_2)

    obs.obs_data_release(browser_settings_1)
    obs.obs_data_release(browser_settings_2)
    obs.obs_data_release(label_settings_1)
    obs.obs_data_release(label_settings_2)
    obs.obs_scene_release(current_scene)

    obs.script_log(obs.LOG_INFO, "Swapped sources for players " .. source1num .. " and " .. source2num)
end

function create_default_scenes()
    if not scene_exists("Main") then
        create_scene("Main")
    end
    if not scene_exists("Intermission") then
        create_scene("Intermission")
    end

    obs.script_log(obs.LOG_INFO, "Created default scenes")
end


function tick()
    local obsstate = get_obsstate()
    if obsstate == nil or obsstate == "" or last_obsstate == obsstate then
        return
    end

    last_obsstate = obsstate

    local instruction, args = string.match(obsstate, "([^:]+):?(.*)")
    obs.script_log(obs.LOG_INFO, "ACCEPTED STATE: " .. obsstate)
    if not instruction then
        obs.script_log(obs.LOG_ERROR, "Invalid obsstate format. Expected <instruction>:<args> or just <instruction>")
        obs.script_log(obs.LOG_ERROR, "OBSSTATE^^^" .. obsstate)
        return
    end

    if args then
        args = split(args, ';')
    else
        args = {}
    end

    local success, message = parse_instr(instruction, args)
    if not success then
        obs.script_log(obs.LOG_ERROR, message)
    end
end

function parse_instr(instruction, args)
    if instruction == "GetAllScenes" then
        local scenes_list = get_all_scene_names()
        local scenes_str = ""

        for _, name in ipairs(scenes_list) do
            scenes_str = scenes_str .. name .. ";"
        end

        write_obsstate_out(scenes_str)

        return true
    end

    if instruction == "SetActiveScene" then
        if not args or #args == 0 then
            return false, "Missing arguments"
        end

        local scene_name = args[1]
        set_scene(scene_name)
        return true
    end

    if instruction == "GenPlayerSources" then
        if not args or #args ~= 2 then
            return false, "Invalid arguments"
        end

        local scene_name, count = args[1], args[2]

        for i = 1, count do
            create_player_group(scene_name, "p" .. i)
        end

        return true
    end

    if instruction == "EditPlayerSource" then
        if not args or #args ~= 4 then
            return false, "Invalid arguments"
        end

        local scene_name, num, ttv_name, player_label = args[1], args[2], args[3], args[4]
        edit_player_source(scene_name, num, ttv_name, player_label)


        return true
    end

    if instruction == "SwapPlayerSources" then
        if not args or #args ~= 3 then
            return false, "Invalid arguments"
        end

        local scene_name, num1, num2 = args[1], args[2], args[3]
        swap_player_sources(scene_name, num1, num2)

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
    --last_obsstate = get_obsstate()
    --obs.script_log(obs.LOG_INFO, last_obsstate)
    create_default_scenes()
    obs.timer_add(tick, REFRESH_RATE_MS)
end

function script_unload()
    obs.timer_remove(tick)
end