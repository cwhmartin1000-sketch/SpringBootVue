<template>
  <div class="employee-page">
    <section class="page-heading">
      <div>
        <span class="eyebrow">Protected workspace</span>
        <h1>Employee directory</h1>
        <p>Use this page to manage the team data protected by JWT roles.</p>
      </div>
      <button v-if="canManage" class="btn btn-primary" @click="startCreate">
        Add employee
      </button>
    </section>

    <div v-if="message" class="alert" :class="messageType">{{ message }}</div>

    <section class="content-grid">
      <div class="panel table-panel">
        <div class="panel-header">
          <div>
            <h2>Employees</h2>
            <span>{{ employees.length }} records</span>
          </div>
          <button class="btn btn-light btn-sm" :disabled="loading" @click="loadEmployees">
            Refresh
          </button>
        </div>

        <div v-if="loading" class="empty-state">Loading employees...</div>
        <div v-else-if="!employees.length" class="empty-state">
          No employees yet. Add the first record.
        </div>
        <div v-else class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Department</th>
                <th>Email</th>
                <th class="text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="employee in employees" :key="employee.id">
                <td><span class="id-badge">{{ employee.employeeId }}</span></td>
                <td>{{ employee.firstName }} {{ employee.lastName }}</td>
                <td>{{ employee.department }}</td>
                <td>{{ employee.email }}</td>
                <td class="text-right">
                  <button v-if="canManage" class="btn btn-link btn-sm" @click="startEdit(employee)">
                    Edit
                  </button>
                  <button
                    v-if="canManage"
                    class="btn btn-link btn-sm text-danger"
                    @click="deleteEmployee(employee)"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="canManage" class="panel form-panel">
        <div class="panel-header">
          <div>
            <h2>{{ editing ? "Edit employee" : "New employee" }}</h2>
            <span>{{ editing ? "Update an existing record" : "Create a directory record" }}</span>
          </div>
        </div>

        <form @submit.prevent="saveEmployee">
          <div class="form-group">
            <label for="employeeId">Employee ID</label>
            <input id="employeeId" v-model.trim="form.employeeId" class="form-control" required maxlength="20" />
          </div>
          <div class="form-row">
            <div class="form-group col-md-6">
              <label for="firstName">First name</label>
              <input id="firstName" v-model.trim="form.firstName" class="form-control" required maxlength="50" />
            </div>
            <div class="form-group col-md-6">
              <label for="lastName">Last name</label>
              <input id="lastName" v-model.trim="form.lastName" class="form-control" required maxlength="50" />
            </div>
          </div>
          <div class="form-group">
            <label for="email">Email</label>
            <input id="email" v-model.trim="form.email" type="email" class="form-control" required maxlength="50" />
          </div>
          <div class="form-group">
            <label for="department">Department</label>
            <input id="department" v-model.trim="form.department" class="form-control" required maxlength="50" />
          </div>
          <div class="form-group">
            <label for="salary">Salary</label>
            <input id="salary" v-model.number="form.salary" type="number" min="0" step="0.01" class="form-control" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-light" @click="resetForm">Clear</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? "Saving..." : editing ? "Save changes" : "Create employee" }}
            </button>
          </div>
        </form>
      </div>
    </section>
  </div>
</template>

<script>
import EmployeeService from "../services/employee.service";

const emptyForm = () => ({
  employeeId: "",
  firstName: "",
  lastName: "",
  email: "",
  department: "",
  salary: null,
});

export default {
  name: "Employee",
  data() {
    return {
      employees: [],
      form: emptyForm(),
      editingId: null,
      loading: false,
      saving: false,
      message: "",
      messageType: "alert-success",
    };
  },
  computed: {
    editing() {
      return this.editingId !== null;
    },
    canManage() {
      return Boolean(this.$store.state.auth.user);
    },
  },
  mounted() {
    this.loadEmployees();
  },
  methods: {
    loadEmployees() {
      this.loading = true;
      EmployeeService.getAll().then(
        (response) => {
          this.employees = response.data;
          this.loading = false;
        },
        (error) => {
          this.showError(error);
          this.loading = false;
        }
      );
    },
    startCreate() {
      this.resetForm();
      window.scrollTo({ top: 0, behavior: "smooth" });
    },
    startEdit(employee) {
      this.editingId = employee.id;
      this.form = {
        employeeId: employee.employeeId,
        firstName: employee.firstName,
        lastName: employee.lastName,
        email: employee.email,
        department: employee.department,
        salary: employee.salary,
      };
    },
    resetForm() {
      this.editingId = null;
      this.form = emptyForm();
    },
    saveEmployee() {
      this.saving = true;
      const request = this.editing
        ? EmployeeService.update(this.editingId, this.form)
        : EmployeeService.create(this.form);

      request.then(
        () => {
          this.showMessage(this.editing ? "Employee updated." : "Employee created.");
          this.resetForm();
          this.loadEmployees();
          this.saving = false;
        },
        (error) => {
          this.showError(error);
          this.saving = false;
        }
      );
    },
    deleteEmployee(employee) {
      if (!window.confirm(`Delete ${employee.firstName} ${employee.lastName}?`)) {
        return;
      }

      EmployeeService.remove(employee.id).then(
        () => {
          this.showMessage("Employee deleted.");
          if (this.editingId === employee.id) {
            this.resetForm();
          }
          this.loadEmployees();
        },
        (error) => this.showError(error)
      );
    },
    showMessage(message) {
      this.message = message;
      this.messageType = "alert-success";
    },
    showError(error) {
      if (error.response && error.response.status === 401) {
        this.message = "登入狀態已失效，請重新登入。";
      } else if (error.response && error.response.status === 403) {
        this.message = "目前帳號沒有新增、修改或刪除員工的權限。";
      } else {
        this.message =
          (error.response && error.response.data && error.response.data.message) ||
          "The request could not be completed.";
      }
      this.messageType = "alert-danger";
    },
  },
};
</script>

<style scoped>
.employee-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-heading,
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.page-heading h1,
.panel-header h2 {
  margin: 0;
  color: #111827;
}

.page-heading p,
.panel-header span {
  margin: 0.45rem 0 0;
  color: #6b7280;
}

.eyebrow {
  color: #2563eb;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(300px, 0.8fr);
  gap: 1.25rem;
}

.panel {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 22px;
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.06);
  padding: 1.4rem;
}

.table-panel {
  min-width: 0;
}

.table {
  margin: 1.25rem 0 0;
}

.table th {
  border-top: 0;
  color: #6b7280;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.table td {
  vertical-align: middle;
}

.id-badge {
  background: #eff6ff;
  border-radius: 8px;
  color: #1d4ed8;
  font-size: 0.82rem;
  font-weight: 700;
  padding: 0.35rem 0.55rem;
}

.empty-state {
  color: #6b7280;
  padding: 2.5rem 1rem;
  text-align: center;
}

.form-panel form {
  margin-top: 1.4rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.7rem;
  margin-top: 1.5rem;
}

@media (max-width: 992px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
