import express from 'express';
import { TaskController } from '../controllers/TaskController.js';

const router = express.Router();
const taskController = new TaskController();

router.get('/', taskController.getAllTasks);
router.post('/', taskController.createTask);
router.get('/:id', taskController.getTaskById);
router.put('/:id', taskController.updateTask);
router.delete('/:id', taskController.deleteTask);
router.post('/:id/complete', taskController.completeTask);

export default router;