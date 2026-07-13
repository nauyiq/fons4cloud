import { createApp } from 'vue'
import { createPinia } from 'pinia'
import {
  Alert,
  Badge,
  Button,
  Collapse,
  Descriptions,
  Divider,
  Drawer,
  Empty,
  Form,
  Input,
  InputNumber,
  Layout,
  Menu,
  Modal,
  Result,
  Select,
  Skeleton,
  Space,
  Spin,
  Switch,
  Table,
  Tabs,
  Tag,
  Timeline,
} from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'
import './styles/tokens.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
;[
  Alert, Badge, Button, Collapse, Descriptions, Divider, Drawer, Empty, Form, Input, InputNumber,
  Layout, Menu, Modal, Result, Select, Skeleton, Space, Spin, Switch, Table, Tabs, Tag, Timeline,
].forEach((component) => app.use(component))
app.mount('#app')
