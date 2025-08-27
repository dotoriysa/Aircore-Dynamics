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
  if (renderer) {
    renderer.dispose();
  }
});

// --- Initialization ---
function init() {
  const container = containerRef.value;
  if (!container) return;

  scene = new THREE.Scene();
  scene.background = new THREE.Color(0x1a2a3a);
  scene.fog = new THREE.Fog(0x1a2a3a, 30, 100);

  camera = new THREE.PerspectiveCamera(60, container.offsetWidth / container.offsetHeight, 0.1, 1000);
  camera.position.set(0, 15, 30);
  camera.lookAt(0, 0, 0);

  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setSize(container.offsetWidth, container.offsetHeight);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  container.appendChild(renderer.domElement);

  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.05;
  controls.screenSpacePanning = false;
  controls.minDistance = 5;
  controls.maxDistance = 50;
  controls.maxPolarAngle = Math.PI / 2;
  controls.target.set(0, 0, 0);

  // ✨ 조명 강도를 높여 더 밝게 수정
  scene.add(new THREE.AmbientLight(0x404040, 2.5)); // 주변광 강화
  const dirLight = new THREE.DirectionalLight(0xffffff, 2.0); // 직사광 강화
  dirLight.position.set(10, 20, 5);
  dirLight.castShadow = true;
  scene.add(dirLight);

  const floor = new THREE.Mesh(
    new THREE.PlaneGeometry(100, 100),
    new THREE.MeshStandardMaterial({ color: 0x2c3e50, roughness: 0.8 })
  );
  floor.rotation.x = -Math.PI / 2;
  floor.receiveShadow = true;
  scene.add(floor);

  createMachines();
  scene.add(factoryObjects);

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
  camera.position.set(0, 15, 30);
  controls.target.set(0, 0, 0);
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
    if (direction === 'zoom-in') {
      controls.dollyIn(1.2);
      return;
    }
    if (direction === 'zoom-out') {
      controls.dollyOut(1.2);
      return;
    }
    const moveSpeed = 1.0;
    const forward = new THREE.Vector3();
    camera.getWorldDirection(forward);
    forward.y = 0;
    forward.normalize();
    const right = new THREE.Vector3().crossVectors(camera.up, forward).normalize();
    let moveVector;
    if (direction === 'w') moveVector = forward;
    if (direction === 's') moveVector = forward.clone().negate();
    if (direction === 'a') moveVector = right;
    if (direction === 'd') moveVector = right.clone().negate();
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

// ✨ 라벨 생성 함수 추가
function createLabel(text) {
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    context.font = 'Bold 40px Arial';
    context.fillStyle = 'rgba(255, 255, 255, 0.95)';
    context.fillText(text, 0, 40);
    const texture = new THREE.CanvasTexture(canvas);
    const spriteMaterial = new THREE.SpriteMaterial({ map: texture, transparent: true });
    const sprite = new THREE.Sprite(spriteMaterial);
    sprite.scale.set(10, 5, 1.0);
    return sprite;
}

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

            // ✨ 생성된 라벨을 머신 그룹에 추가
            const label = createLabel(data.Machine_Name);
            label.position.set(0, 5, 0); // 머신 위쪽에 위치하도록 설정
            machine.add(label);

            factoryObjects.add(machine);
            layout.count++;
        }
    });
}
// --- 재질 색상 원본과 동일하게 수정 ---
function createCastingMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:0x5a6d7c}),f=new THREE.MeshStandardMaterial({color:0xe74c3c}),m=new THREE.Mesh(new THREE.BoxGeometry(6,4,6),b);m.position.y=2,g.add(m);const a=new THREE.Mesh(new THREE.CylinderGeometry(2,2.2,3,32),f);return a.position.y=5.5,g.add(a),g}
function createProcessingMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:0x7f8c8d}),f=new THREE.MeshStandardMaterial({color:0x3498db}),m=new THREE.Mesh(new THREE.BoxGeometry(5,3,4),b);m.position.y=1.5,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(1,4,1),f);return a.position.set(0,3,0),g.add(a),g}
function createInspectionMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:0xbdc3c7}),f=new THREE.MeshStandardMaterial({color:0x2ecc71}),m=new THREE.Mesh(new THREE.BoxGeometry(6,.5,4),b);m.position.y=2,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(.5,3,.5),f);a.position.set(-2.5,3.5,0),g.add(a);const c=new THREE.Mesh(new THREE.BoxGeometry(5,.5,.5),f);return c.position.set(0,5,0),g.add(c),g}
function createAssemblyMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:0x95a5a6}),f=new THREE.MeshStandardMaterial({color:0xf39c12}),m=new THREE.Mesh(new THREE.CylinderGeometry(1.5,1.5,1,32),b);m.position.y=.5,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(.5,3,.5),f);a.position.y=2.5,g.add(a);const c=new THREE.Mesh(new THREE.BoxGeometry(2,.5,.5),f);return c.position.set(1,4,0),g.add(c),g}
function createPackagingMachine(){const g=new THREE.Group(),b=new THREE.MeshStandardMaterial({color:0x34495e}),f=new THREE.MeshStandardMaterial({color:0x8e44ad}),m=new THREE.Mesh(new THREE.BoxGeometry(10,.5,3),b);m.position.y=1,g.add(m);const a=new THREE.Mesh(new THREE.BoxGeometry(3,4,4),f);return a.position.y=3,g.add(a),g}

</script>

<style scoped>
.viewer-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  background: #0f2027;
}
</style>