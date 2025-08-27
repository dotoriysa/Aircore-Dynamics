<template>
  <div ref="containerRef" class="viewer-container"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

// --- Props and Emits ---
const props = defineProps({
  machineInfo: {
    type: Array,
    required: true
  },
  highlightedProcess: {
    type: String,
    default: null
  },
  machineStatuses: { // ✨ 상태 정보 Prop 추가
    type: Object,
    default: () => ({})
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
const animatedParts = []; // ✨ 애니메이션 대상 객체 배열

// --- State ---
let isAnimationRunning = true;
let isTopView = false;
let currentFocusIndex = 0;

const processCameraPositions = {
    '주조': { pos: new THREE.Vector3(-30, 15, 5), target: new THREE.Vector3(-20, 2, 5) },
    '가공': { pos: new THREE.Vector3(-5, 15, 15), target: new THREE.Vector3(-5, 2, 0) },
    '검사': { pos: new THREE.Vector3(10, 12, 15), target: new THREE.Vector3(10, 2, 0) },
    '조립': { pos: new THREE.Vector3(20, 12, 15), target: new THREE.Vector3(20, 2, 0) },
    '포장': { pos: new THREE.Vector3(20, 12, 25), target: new THREE.Vector3(20, 2, 10) }
};

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

  scene.add(new THREE.AmbientLight(0x404040, 2.5));
  const dirLight = new THREE.DirectionalLight(0xffffff, 2.0);
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

// ✨ 애니메이션 루프 수정
function animate() {
  animationId = requestAnimationFrame(animate);
  
  const time = Date.now() * 0.005; // 시간 기반 애니메이션
  
  // 애니메이션 파트 업데이트
  animatedParts.forEach(part => {
    // 기계가 'running' 상태일 때만 애니메이션 적용
    if (part.machine.userData.status === 'running') {
      switch (part.type) {
        case 'press': // 상하 운동
          part.mesh.position.y = part.initialY + Math.sin(time) * 0.5;
          break;
        case 'rotate': // 회전 운동
          part.mesh.rotation.y += 0.05;
          break;
        case 'slide': // 좌우 운동
           part.mesh.position.x = part.initialX + Math.cos(time) * 2;
           break;
      }
    }
  });

  if (controls) controls.update();
  if (renderer) renderer.render(scene, camera);
}


// Prop 변경을 감지하여 설비 가시성을 업데이트
watch(() => props.highlightedProcess, (newProcess) => {
  updateMachineVisibility(newProcess);
});

// ✨ 상태 Prop 변경 감지하여 타워 램프 업데이트
watch(() => props.machineStatuses, (newStatuses) => {
  if (!factoryObjects || factoryObjects.children.length === 0) return;

  factoryObjects.children.forEach(machine => {
    const statusData = newStatuses[machine.userData.PM_ID];
    if (statusData) {
      machine.userData.status = statusData.status; // 애니메이션을 위해 상태 저장
      const lamp = machine.getObjectByName('towerLamp');
      if (lamp) {
        updateTowerLamp(lamp, statusData.status);
      }
    }
  });
}, { deep: true });


// 설비 가시성 업데이트 함수
function updateMachineVisibility(processName) {
    if (!factoryObjects || factoryObjects.children.length === 0) return;
    factoryObjects.children.forEach(machine => {
        const isTarget = machine.userData.Process_Name === processName;
        machine.traverse(child => {
            if (child.isMesh) {
                child.material.transparent = true;
                if (processName === null) {
                    child.material.opacity = 1.0;
                } else {
                    child.material.opacity = isTarget ? 1.0 : 0.15;
                }
            }
        });
    });
}

// 특정 공정에 카메라 포커스를 맞추는 함수
function focusOnProcess(processName) {
    const view = processCameraPositions[processName];
    if (view && camera && controls) {
        const startPos = camera.position.clone();
        const startTarget = controls.target.clone();
        let duration = 500;
        let startTime = null;
        function move(time) {
            if (startTime === null) startTime = time;
            const elapsed = time - startTime;
            const t = Math.min(elapsed / duration, 1);
            camera.position.lerpVectors(startPos, view.pos, t);
            controls.target.lerpVectors(startTarget, view.target, t);
            if (t < 1) requestAnimationFrame(move);
        }
        requestAnimationFrame(move);
    } else {
        resetView();
    }
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
  focusOnProcess(null);
  updateMachineVisibility(props.highlightedProcess);
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
  const relevantObjects = props.highlightedProcess
    ? factoryObjects.children.filter(m => m.userData.Process_Name === props.highlightedProcess)
    : factoryObjects.children;
  if (relevantObjects.length === 0) return;
  currentFocusIndex = (currentFocusIndex + 1) % relevantObjects.length;
  const equipment = relevantObjects[currentFocusIndex];
  const pos = new THREE.Vector3();
  equipment.getWorldPosition(pos);
  camera.position.set(pos.x + 8, pos.y + 8, pos.z + 8);
  controls.target.copy(pos);
  emit('object-selected', equipment.userData);
  highlightObject(equipment);
  controls.autoRotate = false;
  isTopView = false;
}
function toggleAutoRotate() {
  controls.autoRotate = !controls.autoRotate;
  if (controls.autoRotate) isTopView = false;
}
function moveCamera(direction) {
    if (direction === 'zoom-in') { controls.dollyIn(1.2); return; }
    if (direction === 'zoom-out') { controls.dollyOut(1.2); return; }
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
  toggleAnimation, resetView, toggleTopView, focusNextEquipment,
  toggleAutoRotate, moveCamera, focusOnProcess, handleResize 
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
    const isVisible = object.children[0].material.opacity > 0.5;
    if (object.userData.PM_ID && isVisible) {
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

// ✨ 타워 램프 생성 함수
function createTowerLamp() {
  const lampGroup = new THREE.Group();
  lampGroup.name = 'towerLamp';

  const createLight = (color, y) => {
    const material = new THREE.MeshStandardMaterial({ color: color, emissive: 0x000000 });
    const mesh = new THREE.Mesh(new THREE.CylinderGeometry(0.2, 0.2, 0.4, 16), material);
    mesh.position.y = y;
    return mesh;
  };

  const pole = new THREE.Mesh(
    new THREE.CylinderGeometry(0.05, 0.05, 1.5, 8),
    new THREE.MeshStandardMaterial({ color: 0x888888 })
  );
  pole.position.y = 0.75;
  lampGroup.add(pole);

  const greenLight = createLight(0x00ff00, 1.3);
  greenLight.name = 'green';
  lampGroup.add(greenLight);

  const yellowLight = createLight(0xffff00, 0.8);
  yellowLight.name = 'yellow';
  lampGroup.add(yellowLight);

  const redLight = createLight(0xff0000, 0.3);
  redLight.name = 'red';
  lampGroup.add(redLight);
  
  return lampGroup;
}

// ✨ 타워 램프 업데이트 함수
function updateTowerLamp(lamp, status) {
  const lights = {
    green: lamp.getObjectByName('green'),
    yellow: lamp.getObjectByName('yellow'),
    red: lamp.getObjectByName('red'),
  };
  
  // 모든 불을 끈다 (emissive 0)
  Object.values(lights).forEach(light => light.material.emissive.setHex(0x000000));

  // 상태에 맞는 불을 켠다
  if (status === 'running' && lights.green) {
    lights.green.material.emissive.setHex(0x00ff00);
  } else if (status === 'idle' && lights.yellow) {
    lights.yellow.material.emissive.setHex(0xffff00);
  } else if (status === 'stopped' && lights.red) {
    lights.red.material.emissive.setHex(0xff0000);
  }
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
            
            const label = createLabel(data.Machine_Name);
            label.position.set(0, 5, 0);
            machine.add(label);

            // ✨ 모든 기계에 타워 램프 추가
            const towerLamp = createTowerLamp();
            towerLamp.position.set(2.5, 4, 0);
            machine.add(towerLamp);

            factoryObjects.add(machine);
            layout.count++;
        }
    });
    updateMachineVisibility(props.highlightedProcess);
}

// --- 기계 생성 함수들 수정 (애니메이션 파트 추가) ---
function createCastingMachine(){
  const g = new THREE.Group();
  const b = new THREE.MeshStandardMaterial({color:0x5a6d7c});
  const f = new THREE.MeshStandardMaterial({color:0xe74c3c});
  const m = new THREE.Mesh(new THREE.BoxGeometry(6,4,6),b);
  m.position.y=2;
  g.add(m);
  const a = new THREE.Mesh(new THREE.CylinderGeometry(2,2.2,3,32),f);
  a.position.y=5.5;
  g.add(a);
  // ✨ 애니메이션 파트 등록
  animatedParts.push({ mesh: a, type: 'press', initialY: a.position.y, machine: g });
  return g;
}

function createProcessingMachine(){
  const g = new THREE.Group();
  const b = new THREE.MeshStandardMaterial({color:0x7f8c8d});
  const f = new THREE.MeshStandardMaterial({color:0x3498db});
  const m = new THREE.Mesh(new THREE.BoxGeometry(5,3,4),b);
  m.position.y=1.5;
  g.add(m);
  const a = new THREE.Mesh(new THREE.BoxGeometry(1,4,1),f);
  a.position.set(0,3,0);
  g.add(a);
  // ✨ 애니메이션 파트 등록
  animatedParts.push({ mesh: a, type: 'rotate', machine: g });
  return g;
}

function createInspectionMachine(){
  const g=new THREE.Group();
  const b=new THREE.MeshStandardMaterial({color:0xbdc3c7});
  const f=new THREE.MeshStandardMaterial({color:0x2ecc71});
  const m=new THREE.Mesh(new THREE.BoxGeometry(6,.5,4),b);
  m.position.y=2;
  g.add(m);
  const a=new THREE.Mesh(new THREE.BoxGeometry(.5,3,.5),f);
  a.position.set(-2.5,3.5,0);
  g.add(a);
  const c=new THREE.Mesh(new THREE.BoxGeometry(5,.5,.5),f);
  c.position.set(0,5,0);
  g.add(c);
  // ✨ 애니메이션 파트 등록
  animatedParts.push({ mesh: c, type: 'slide', initialX: c.position.x, machine: g });
  return g;
}
function createAssemblyMachine(){
  const g=new THREE.Group();
  const b=new THREE.MeshStandardMaterial({color:0x95a5a6});
  const f=new THREE.MeshStandardMaterial({color:0xf39c12});
  const m=new THREE.Mesh(new THREE.CylinderGeometry(1.5,1.5,1,32),b);
  m.position.y=.5;
  g.add(m);
  const a=new THREE.Mesh(new THREE.BoxGeometry(.5,3,.5),f);
  a.position.y=2.5;
  g.add(a);
  const c=new THREE.Mesh(new THREE.BoxGeometry(2,.5,.5),f);
  c.position.set(1,4,0);
  g.add(c);
  // ✨ 애니메이션 파트 등록
  animatedParts.push({ mesh: a, type: 'press', initialY: a.position.y, machine: g });
  return g;
}
function createPackagingMachine(){
  const g=new THREE.Group();
  const b=new THREE.MeshStandardMaterial({color:0x34495e});
  const f=new THREE.MeshStandardMaterial({color:0x8e44ad});
  const m=new THREE.Mesh(new THREE.BoxGeometry(10,.5,3),b);
  m.position.y=1;
  g.add(m);
  const a=new THREE.Mesh(new THREE.BoxGeometry(3,4,4),f);
  a.position.y=3;
  g.add(a);
  // ✨ 애니메이션 파트 등록
  animatedParts.push({ mesh: a, type: 'press', initialY: a.position.y, machine: g });
  return g;
}

</script>

<style scoped>
.viewer-container {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  background: #0f2027;
}
</style>