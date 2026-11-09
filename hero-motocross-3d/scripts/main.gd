extends Node3D

var bike: Node3D
var speed := 0.0
var steer := 0.0
var bike_names := ["Splendor+", "HF Deluxe", "Passion+", "Glamour", "Super Splendor", "Xtreme 125R", "Xtreme 160R", "Xtreme 200S", "Xpulse 200 4V", "Karizma XMR", "Mavrick 440", "Destini", "Pleasure+", "Xoom"]
var bike_index := 0
var hud: Label
var touch_left := false
var touch_right := false
var touch_gas := false
var touch_brake := false

func _ready() -> void:
	_build_world()
	_build_bike()
	_build_ui()
	_setup_input()
	Input.set_mouse_mode(Input.MOUSE_MODE_VISIBLE)

func _setup_input() -> void:
	for action in ["gas", "brake", "left", "right"]:
		if not InputMap.has_action(action):
			InputMap.add_action(action)
	InputMap.action_add_event("gas", _key(KEY_W))
	InputMap.action_add_event("gas", _key(KEY_UP))
	InputMap.action_add_event("brake", _key(KEY_S))
	InputMap.action_add_event("brake", _key(KEY_DOWN))
	InputMap.action_add_event("left", _key(KEY_A))
	InputMap.action_add_event("left", _key(KEY_LEFT))
	InputMap.action_add_event("right", _key(KEY_D))
	InputMap.action_add_event("right", _key(KEY_RIGHT))

func _key(code: Key) -> InputEventKey:
	var event := InputEventKey.new()
	event.physical_keycode = code
	return event

func _build_world() -> void:
	var world_env := WorldEnvironment.new()
	var env := Environment.new()
	env.background_mode = Environment.BG_SKY
	env.sky = Sky.new()
	env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	env.ambient_light_color = Color(0.72, 0.78, 0.86)
	env.ambient_light_energy = 0.8
	world_env.environment = env
	add_child(world_env)
	var sun := DirectionalLight3D.new()
	sun.rotation_degrees = Vector3(-48, -30, 0)
	sun.light_energy = 1.25
	add_child(sun)
	_box("Ground", Vector3(0, -0.35, -25), Vector3(80, 0.5, 180), Color(0.20, 0.43, 0.17))
	_box("Road", Vector3(0, -0.05, -25), Vector3(9, 0.12, 180), Color(0.18, 0.19, 0.20))
	for z in range(-100, 60, 10):
		_box("RoadMark", Vector3(0, 0.02, z), Vector3(0.16, 0.02, 4), Color(0.94, 0.88, 0.60))
		for side in [-1, 1]:
			_tree(Vector3(side * randf_range(7, 16), 0, z + randf_range(-3, 3)))

func _box(label: String, pos: Vector3, size: Vector3, color: Color) -> MeshInstance3D:
	var node := MeshInstance3D.new()
	node.name = label
	var mesh := BoxMesh.new()
	mesh.size = size
	node.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	node.material_override = mat
	node.position = pos
	add_child(node)
	return node

func _tree(pos: Vector3) -> void:
	var trunk := _box("TreeTrunk", pos + Vector3(0, 1, 0), Vector3(0.35, 2, 0.35), Color(0.30, 0.18, 0.09))
	var crown := MeshInstance3D.new()
	var mesh := SphereMesh.new()
	mesh.radius = 1.15
	mesh.height = 2.3
	crown.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = Color(0.08, randf_range(0.28, 0.43), 0.12)
	crown.material_override = mat
	crown.position = pos + Vector3(0, 2.5, 0)
	add_child(crown)

func _build_bike() -> void:
	bike = Node3D.new()
	bike.position = Vector3(0, 0.7, 0)
	add_child(bike)
	_cylinder(bike, "RearWheel", Vector3(0, -0.45, 0.85), 0.48, 0.18, Color(0.035, 0.035, 0.04), true)
	_cylinder(bike, "FrontWheel", Vector3(0, -0.45, -0.95), 0.48, 0.18, Color(0.035, 0.035, 0.04), true)
	_box_child(bike, "Frame", Vector3(0, 0.08, 0), Vector3(0.16, 0.18, 1.45), Color(0.82, 0.08, 0.04))
	_box_child(bike, "Tank", Vector3(0, 0.42, -0.12), Vector3(0.48, 0.42, 0.68), Color(0.88, 0.06, 0.035))
	_box_child(bike, "Seat", Vector3(0, 0.56, 0.55), Vector3(0.38, 0.14, 0.72), Color(0.06, 0.06, 0.07))
	_box_child(bike, "Handlebar", Vector3(0, 0.78, -0.9), Vector3(1.0, 0.08, 0.10), Color(0.12, 0.12, 0.13))
	_box_child(bike, "Headlight", Vector3(0, 0.42, -1.02), Vector3(0.32, 0.25, 0.12), Color(1.0, 0.88, 0.56))

func _cylinder(parent: Node3D, label: String, pos: Vector3, radius: float, height: float, color: Color, wheel: bool = false) -> void:
	var node := MeshInstance3D.new()
	node.name = label
	var mesh := CylinderMesh.new()
	mesh.top_radius = radius
	mesh.bottom_radius = radius
	mesh.height = height
	node.mesh = mesh
	node.rotation_degrees = Vector3(90, 0, 0) if wheel else Vector3.ZERO
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	node.material_override = mat
	node.position = pos
	parent.add_child(node)

func _box_child(parent: Node3D, label: String, pos: Vector3, size: Vector3, color: Color) -> void:
	var node := MeshInstance3D.new()
	node.name = label
	var mesh := BoxMesh.new()
	mesh.size = size
	node.mesh = mesh
	var mat := StandardMaterial3D.new()
	mat.albedo_color = color
	node.material_override = mat
	node.position = pos
	parent.add_child(node)

func _build_ui() -> void:
	var camera := Camera3D.new()
	camera.position = Vector3(0, 3.4, 7.5)
	camera.rotation_degrees = Vector3(-12, 0, 0)
	camera.current = true
	add_child(camera)
	var layer := CanvasLayer.new()
	add_child(layer)
	hud = Label.new()
	hud.position = Vector2(24, 18)
	hud.add_theme_font_size_override("font_size", 25)
	hud.add_theme_color_override("font_color", Color.WHITE)
	layer.add_child(hud)
	_add_button(layer, "◀", Vector2(24, -105), "left")
	_add_button(layer, "▶", Vector2(112, -105), "right")
	_add_button(layer, "BRAKE", Vector2(-190, -105), "brake")
	_add_button(layer, "GAS", Vector2(-100, -105), "gas")
	var change := Button.new()
	change.text = "CHANGE BIKE"
	change.position = Vector2(24, 78)
	change.size = Vector2(160, 48)
	change.pressed.connect(_next_bike)
	layer.add_child(change)

func _add_button(layer: CanvasLayer, caption: String, offset: Vector2, action: String) -> void:
	var b := Button.new()
	b.text = caption
	b.size = Vector2(78, 68)
	b.anchor_left = 1.0 if offset.x < 0 else 0.0
	b.anchor_right = 1.0 if offset.x < 0 else 0.0
	b.anchor_top = 1.0
	b.anchor_bottom = 1.0
	b.position = offset
	b.button_down.connect(func(): _set_touch(action, true))
	b.button_up.connect(func(): _set_touch(action, false))
	layer.add_child(b)

func _set_touch(action: String, value: bool) -> void:
	match action:
		"gas": touch_gas = value
		"brake": touch_brake = value
		"left": touch_left = value
		"right": touch_right = value

func _next_bike() -> void:
	bike_index = (bike_index + 1) % bike_names.size()

func _process(delta: float) -> void:
	var accelerating := Input.is_action_pressed("gas") or touch_gas
	var braking := Input.is_action_pressed("brake") or touch_brake
	var left := Input.is_action_pressed("left") or touch_left
	var right := Input.is_action_pressed("right") or touch_right
	speed = move_toward(speed, 85.0 if accelerating else 0.0, (24.0 if accelerating else 12.0) * delta)
	if braking:
		speed = move_toward(speed, 0.0, 55.0 * delta)
	steer = lerp(steer, float(right) - float(left), delta * 5.0)
	bike.position.x = clampf(bike.position.x + steer * delta * (1.0 + speed / 35.0), -3.2, 3.2)
	bike.rotation.z = lerp(bike.rotation.z, -steer * 0.18, delta * 5.0)
	bike.rotation.x = sin(Time.get_ticks_msec() * 0.006) * 0.012
	hud.text = "HERO MOTOCROSS 3D   |   %s\nSPEED: %03d km/h" % [bike_names[bike_index], int(speed)]
