<template>
  <div ref="containerRef" class="viewer-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

// --- Props and Emits ---
const props = defineProps({
  machineInfo: {
    type: Array,
    required: true
  }
});
const emit = defineEmits(['object-selected']);

// --- 3D Scene Refs ---
const containerRef = ref(null);
let scene, camera, renderer, controls, raycaster, mouse;
let animationId;
const factoryObjects = new THREE.Group();
let selectedObject = null;
const initialColors = new Map();

// --- State ---
let isAnimationRunning = true;
let isTopView = false;
let currentFocusIndex = 0;


// --- Lifecycle Hooks ---
onMounted(() => {
  init();
  animate();
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  cancelAnimationFrame(animationId);
  window.removeEventListener('resize', handleResize);
  // Clean up Three.js resources
  if (renderer) {
    renderer.dispose();
  }
});

// --- Initialization ---
function init() {
  const container = containerRef.value;
  if (!container) return;

  // Scene
  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x1a2a3a);
  scene.fog = new THREE.Fog(0x1a2a3a, 40, 120);

  // Camera
  camera = new THREE.PerspectiveCamera(60, container.offsetWidth / container.offsetHeight, 0.1, 1000);
  camera.position.set(25, 15, 25);

  // Renderer
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(container.offsetWidth, container.offsetHeight);
  renderer.shadowMap.enabled = true;
  container.appendChild(renderer.domElement);

  // Controls
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.target.set(0, 2, 0);
  camera.lookAt(controls.target);

  // Lights
  scene.add(new THREE.AmbientLight(0x404040, 1.5));
  const dirLight = new THREE.DirectionalLight(0xffffff, 1.5);
  dirLight.position.set(10, 20, 5);
  dirLight.castShadow = true;
  scene.add(dirLight);

  // Floor
  const floor = new THREE.Mesh(
    new THREE.PlaneGeometry(100, 100),
    new THREE.MeshStandardMaterial({ color: 0x2c3e50 })
  );
  floor.rotation.x = -Math.PI / 2;
  floor.receiveShadow = true;
  scene.add(floor);

  // Machines
  createMachines();
  scene.add(factoryObjects);

  // Interaction
  raycaster = new THREE.Raycaster();
  mouse = new THREE.Vector2();
  container.addEventListener('click', onMouseClick);
}


// --- Animation Loop ---
function animate() {
  animationId = requestAnimationFrame(animate);
  if (controls) controls.update();
  if (renderer) renderer.render(scene, camera);
}


// --- Public Methods (exposed to parent) ---
function toggleAnimation() {
  isAnimationRunning = !isAnimationRunning;
  if (isAnimationRunning) {
    animate();
  } else {
    cancelAnimationFrame(animationId);
  }
  return isAnimationRunning;
}

function resetView() {
  isTopView = false;
  controls.autoRotate = false;
  camera.position.set(25, 15, 25);
  controls.target.set(0, 2, 0);
}

function toggleTopView() {
  isTopView = !isTopView;
  controls.autoRotate = false;
  if (isTopView) {
    camera.position.set(0, 45, 0);
    controls.target.set(0, 2, 0);
  } else {
    resetView();
  }
}

function focusNextEquipment() {
  if (factoryObjects.children.length === 0) return;

  const equipment = factoryObjects.children[currentFocusIndex];
  const pos = new THREE.Vector3();
  equipment.getWorldPosition(pos);
  
  camera.position.set(pos.x + 8, pos.y + 8, pos.z + 8);
  controls.target.copy(pos);
  
  emit('object-selected', equipment.userData);
  highlightObject(equipment);

  currentFocusIndex = (currentFocusIndex + 1) % factoryObjects.children.length;
  controls.autoRotate = false;
  isTopView = false;
}

function toggleAutoRotate() {
  controls.autoRotate = !controls.autoRotate;
  if (controls.autoRotate) isTopView = false;
}

function moveCamera(direction) {
    const moveSpeed = 1.0;
    const forward = new THREE.Vector3();
    camera.getWorldDirection(forward);
    forward.y = 0;
    forward.normalize();
    const right = new THREE.Vector3().crossVectors(camera.up, forward).normalize();
    
    let moveVector;
    if (direction === 'w') moveVector = forward;
    if (direction === 's') moveVector = forward.clone().negate();
    if (direction === 'a') moveVector = right.clone().negate();
    if (direction === 'd') moveVector = right;

    if (moveVector) {
        camera.position.addScaledVector(moveVector, moveSpeed);
        controls.target.addScaledVector(moveVector, moveSpeed);
    } else if (direction === 'q') {
        camera.position.y += moveSpeed;
        controls.target.y += moveSpeed;
    } else if (direction === 'e') {
        camera.position.y -= moveSpeed;
        controls.target.y -= moveSpeed;
    }
    controls.autoRotate = false;
}

defineExpose({
  toggleAnimation,
  resetView,
  toggleTopView,
  focusNextEquipment,
  toggleAutoRotate,
  moveCamera
});


// --- Internal Functions ---
function handleResize() {
  if (camera && renderer) {
    const container = containerRef.value;
    camera.aspect = container.offsetWidth / container.offsetHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.offsetWidth, container.offsetHeight);
  }
}

function onMouseClick(event) {
  const container = containerRef.value;
  const rect = container.getBoundingClientRect();
  mouse.x = ((event.clientX - rect.left) / container.clientWidth) * 2 - 1;
  mouse.y = -((event.clientY - rect.top) / container.clientHeight) * 2 + 1;

  raycaster.setFromCamera(mouse, camera);
  const intersects = raycaster.intersectObjects(factoryObjects.children, true);

  if (intersects.length > 0) {
    let object = intersects[0].object;
    while (object.parent && !object.userData.PM_ID) {
      object = object.parent;
    }
    if (object.userData.PM_ID) {
      highlightObject(object);
      emit('object-selected', object.userData);
    }
  } else {
    highlightObject(null);
    emit('object-selected', null);
  }
}

function highlightObject(object) {
    if (selectedObject) {
        selectedObject.traverse(child => {
            if (child.isMesh) {
                child.material.emissive.setHex(initialColors.get(child.uuid) || 0x000000);
            }
        });
    }

    selectedObject = object;

    if (selectedObject) {
        selectedObject.traverse(child => {
            if (child.isMesh) {
                initialColors.set(child.uuid, child.material.emissive.getHex());
                child.material.emissive.setHex(0x555555);
            }
        });
    }
}

// --- Machine Creation (same as original, but uses props.machineInfo) ---
function createMachines() {
    const machineLayout = { '주조': { x: -20, z: -5, count: 0 }, '가공': { x: -5, z: -5, count: 0 }, '검사': { x: 10, z: 0, count: 0 }, '조립': { x: 20, z: 0, count: 0 }, '포장': { x: 20, z: 10, count: 0 } };
    props.machineInfo.forEach(data => {
        const layout = machineLayout[data.Process_Name];
        if (!layout) return;
        let machine;
        switch (data.Process_Name) {
            case '주조': machine = createCastingMachine(); break;
            case '가공': machine = createProcessingMachine(); break;
            case '검사': machine = createInspectionMachine(); break;
            case '조립': machine = createAssemblyMachine(); break;
            case '포장': machine = createPackagingMachine(); break;
        }
        if (machine) {
            machine.position.set(layout.x, 0, layout.z + layout.count * 8);
            machine.userData = data;
            // Labels can be added here if needed
            factoryObjects.add(machine);
            layout.count++;
        }
    });
}
// Functions for creating machine shapes (createCastingMachine, etc.)
// These are direct copies from the original HTML file's script
function createCastingMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:15158268,roughness:.6}),f=new THREE.MeshStandardMaterial({color:5926396,metalness:.8}),m=new THREE.Mesh(new THREE.BoxGeometry(6,4,6),b);m.position.y=2,g.add(m);const a=new THREE.Mesh(new THREE.CylinderGeometry(2,2.2,3,32),f);return a.position.y=5.5,g.add(a),g}
function createProcessingMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:3447003,roughness:.5}),f=new THREE.MeshStandardMaterial({color:8359053,metalness:.7}),m=new THREE.Mesh(new THREE.BoxGeometry(5,3,4),b);m.position.y=1.5,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(1,4,1),f);return a.position.set(0,3,0),g.add(a),g}
function createInspectionMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:12434871}),f=new THREE.MeshStandardMaterial({color:3066379,emissive:1127185}),m=new THREE.Mesh(new THREE.BoxGeometry(6,.5,4),b);m.position.y=2,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(.5,3,.5),f);a.position.set(-2.5,3.5,0),g.add(a);const c=new THREE.Mesh(new THREE.BoxGeometry(5,.5,.5),f);return c.position.set(0,5,0),g.add(c),g}
function createAssemblyMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:9807254}),f=new THREE.MeshStandardMaterial({color:15998482,roughness:.3}),m=new THREE.Mesh(new THREE.CylinderGeometry(1.5,1.5,1,32),b);m.position.y=.5,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(.5,3,.5),f);a.position.y=2.5,g.add(a);const c=new THREE.Mesh(new THREE.BoxGeometry(2,.5,.5),f);return c.position.set(1,4,0),g.add(c),g}
function createPackagingMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:3426654}),f=new THREE.MeshStandardMaterial({color:9322925,roughness:.7}),m=new THREE.Mesh(new THREE.BoxGeometry(10,.5,3),b);m.position.y=1,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(3,4,4),f);return a.position.y=3,g.add(a),g}

</script>

<style scoped>
.viewer-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  background: #0f2027;
}
</style>