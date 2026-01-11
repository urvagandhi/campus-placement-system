# UI Components Library

Complete documentation for reusable UI components in PlacementPro frontend.

## Table of Contents

1. [Overview](#overview)
2. [Component List](#component-list)
3. [Button](#button)
4. [Input](#input)
5. [Card](#card)
6. [Badge](#badge)
7. [Logo](#logo)
8. [Modal](#modal)
9. [Design Tokens](#design-tokens)

---

## Overview

The component library provides:

- **Consistent styling**: All components follow the design system
- **Accessibility**: ARIA attributes and keyboard navigation
- **Flexibility**: Props for customization
- **TypeScript-ready**: JSDoc type annotations

### Design Philosophy

- **Glassmorphism**: Frosted glass effect with backdrop blur
- **Gradients**: Subtle gradient backgrounds
- **Animations**: Smooth transitions and micro-interactions
- **Responsiveness**: Mobile-first design

---

## Component List

| Component | Location | Description |
|-----------|----------|-------------|
| Button | `src/components/ui/Button.jsx` | Primary action button |
| Input | `src/components/ui/Input.jsx` | Text input with validation |
| Card | `src/components/ui/Card.jsx` | Content container |
| Badge | `src/components/ui/Badge.jsx` | Status indicator |
| Logo | `src/components/ui/Logo.jsx` | App logo |
| Modal | `src/components/ui/Modal.jsx` | Dialog overlay |

---

## Button

### Usage

```jsx
import Button from '@/components/ui/Button';

// Primary button
<Button>Click Me</Button>

// Secondary button
<Button variant="secondary">Cancel</Button>

// Ghost button
<Button variant="ghost">Learn More</Button>

// Loading state
<Button loading>Submitting...</Button>

// Disabled
<Button disabled>Not Available</Button>

// With icon
<Button>
  <PlusIcon /> Add Item
</Button>

// Full width
<Button fullWidth>Submit</Button>

// Sizes
<Button size="sm">Small</Button>
<Button size="md">Medium</Button>
<Button size="lg">Large</Button>
<Button size="xl">Extra Large</Button>
```

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `variant` | `'primary' \| 'secondary' \| 'ghost' \| 'danger'` | `'primary'` | Button style |
| `size` | `'sm' \| 'md' \| 'lg' \| 'xl'` | `'md'` | Button size |
| `loading` | `boolean` | `false` | Show loading spinner |
| `disabled` | `boolean` | `false` | Disable button |
| `fullWidth` | `boolean` | `false` | Full width button |
| `type` | `'button' \| 'submit' \| 'reset'` | `'button'` | HTML type |
| `onClick` | `function` | - | Click handler |
| `className` | `string` | - | Additional classes |
| `children` | `ReactNode` | - | Button content |

### Implementation

```jsx
import { forwardRef } from 'react';

const Button = forwardRef(({
    variant = 'primary',
    size = 'md',
    loading = false,
    disabled = false,
    fullWidth = false,
    type = 'button',
    onClick,
    className = '',
    children,
    ...props
}, ref) => {
    const baseStyles = 'inline-flex items-center justify-center font-semibold transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2';

    const variants = {
        primary: 'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500 shadow-lg shadow-indigo-500/25',
        secondary: 'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 focus:ring-indigo-500',
        ghost: 'text-gray-600 hover:bg-gray-100 hover:text-gray-900 focus:ring-gray-500',
        danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
    };

    const sizes = {
        sm: 'px-3 py-1.5 text-sm rounded-lg',
        md: 'px-4 py-2 text-sm rounded-xl',
        lg: 'px-5 py-2.5 text-base rounded-xl',
        xl: 'px-6 py-3 text-lg rounded-2xl',
    };

    return (
        <button
            ref={ref}
            type={type}
            onClick={onClick}
            disabled={disabled || loading}
            className={`
                ${baseStyles}
                ${variants[variant]}
                ${sizes[size]}
                ${fullWidth ? 'w-full' : ''}
                ${disabled ? 'opacity-50 cursor-not-allowed' : ''}
                ${className}
            `}
            {...props}
        >
            {loading && <LoadingSpinner className="mr-2 h-4 w-4" />}
            {children}
        </button>
    );
});
```

---

## Input

### Usage

```jsx
import Input from '@/components/ui/Input';

// Basic input
<Input
    label="Email"
    name="email"
    type="email"
    placeholder="you@example.com"
/>

// With error
<Input
    label="Password"
    name="password"
    type="password"
    error="Password is required"
/>

// Password with toggle
<Input
    label="Password"
    name="password"
    type="password"
    showPasswordToggle
/>

// With helper text
<Input
    label="Username"
    name="username"
    helperText="This will be your public display name"
/>

// Required field
<Input
    label="Full Name"
    name="name"
    required
/>
```

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `label` | `string` | - | Input label |
| `name` | `string` | - | Input name |
| `type` | `string` | `'text'` | Input type |
| `placeholder` | `string` | - | Placeholder text |
| `error` | `string` | - | Error message |
| `helperText` | `string` | - | Helper text |
| `required` | `boolean` | `false` | Required field |
| `disabled` | `boolean` | `false` | Disabled input |
| `showPasswordToggle` | `boolean` | `false` | Show password toggle |
| `value` | `string` | - | Input value |
| `onChange` | `function` | - | Change handler |

---

## Card

### Usage

```jsx
import Card from '@/components/ui/Card';

// Basic card
<Card>
    <h3>Card Title</h3>
    <p>Card content goes here.</p>
</Card>

// Hover effect
<Card hover>
    Hover me!
</Card>

// Glass effect
<Card glass>
    Glassmorphism card
</Card>

// With padding variants
<Card padding="sm">Small padding</Card>
<Card padding="md">Medium padding</Card>
<Card padding="lg">Large padding</Card>
<Card padding="none">No padding</Card>
```

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `hover` | `boolean` | `false` | Hover lift effect |
| `glass` | `boolean` | `false` | Glassmorphism effect |
| `padding` | `'none' \| 'sm' \| 'md' \| 'lg'` | `'md'` | Padding size |
| `className` | `string` | - | Additional classes |
| `children` | `ReactNode` | - | Card content |

---

## Badge

### Usage

```jsx
import Badge from '@/components/ui/Badge';

// Status badges
<Badge variant="success">Active</Badge>
<Badge variant="warning">Pending</Badge>
<Badge variant="danger">Rejected</Badge>
<Badge variant="info">New</Badge>
<Badge variant="default">Draft</Badge>

// Sizes
<Badge size="sm">Small</Badge>
<Badge size="md">Medium</Badge>
<Badge size="lg">Large</Badge>

// With icon
<Badge variant="success">
    <CheckIcon /> Approved
</Badge>
```

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `variant` | `'default' \| 'success' \| 'warning' \| 'danger' \| 'info'` | `'default'` | Badge color |
| `size` | `'sm' \| 'md' \| 'lg'` | `'md'` | Badge size |
| `className` | `string` | - | Additional classes |
| `children` | `ReactNode` | - | Badge content |

### Color Mapping

| Variant | Background | Text |
|---------|------------|------|
| `default` | `bg-gray-100` | `text-gray-700` |
| `success` | `bg-emerald-100` | `text-emerald-700` |
| `warning` | `bg-amber-100` | `text-amber-700` |
| `danger` | `bg-red-100` | `text-red-700` |
| `info` | `bg-blue-100` | `text-blue-700` |

---

## Logo

### Usage

```jsx
import Logo from '@/components/ui/Logo';

// Default logo
<Logo />

// Size variants
<Logo size="sm" />
<Logo size="md" />
<Logo size="lg" />

// Text visibility
<Logo showText={true} />
<Logo showText={false} />

// Click handler
<Logo onClick={() => router.push('/')} />
```

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `size` | `'sm' \| 'md' \| 'lg'` | `'md'` | Logo size |
| `showText` | `boolean` | `true` | Show app name |
| `onClick` | `function` | - | Click handler |
| `className` | `string` | - | Additional classes |

---

## Modal

### Usage

```jsx
import Modal from '@/components/ui/Modal';

const [isOpen, setIsOpen] = useState(false);

<Modal
    isOpen={isOpen}
    onClose={() => setIsOpen(false)}
    title="Confirm Action"
>
    <p>Are you sure you want to proceed?</p>
    <div className="flex gap-3 mt-4">
        <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
        </Button>
        <Button onClick={handleConfirm}>
            Confirm
        </Button>
    </div>
</Modal>
```

### Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `isOpen` | `boolean` | `false` | Modal visibility |
| `onClose` | `function` | - | Close handler |
| `title` | `string` | - | Modal title |
| `size` | `'sm' \| 'md' \| 'lg' \| 'xl'` | `'md'` | Modal size |
| `closeOnOverlay` | `boolean` | `true` | Close on overlay click |
| `children` | `ReactNode` | - | Modal content |

---

## Design Tokens

### Colors

```css
/* Primary */
--color-primary-50: #eef2ff;
--color-primary-100: #e0e7ff;
--color-primary-500: #6366f1;
--color-primary-600: #4f46e5;
--color-primary-700: #4338ca;

/* Success */
--color-success-50: #ecfdf5;
--color-success-500: #10b981;
--color-success-700: #047857;

/* Warning */
--color-warning-50: #fffbeb;
--color-warning-500: #f59e0b;
--color-warning-700: #b45309;

/* Danger */
--color-danger-50: #fef2f2;
--color-danger-500: #ef4444;
--color-danger-700: #b91c1c;
```

### Spacing

```css
--spacing-1: 0.25rem;  /* 4px */
--spacing-2: 0.5rem;   /* 8px */
--spacing-3: 0.75rem;  /* 12px */
--spacing-4: 1rem;     /* 16px */
--spacing-5: 1.25rem;  /* 20px */
--spacing-6: 1.5rem;   /* 24px */
--spacing-8: 2rem;     /* 32px */
```

### Border Radius

```css
--radius-sm: 0.375rem;  /* 6px */
--radius-md: 0.5rem;    /* 8px */
--radius-lg: 0.75rem;   /* 12px */
--radius-xl: 1rem;      /* 16px */
--radius-2xl: 1.5rem;   /* 24px */
--radius-full: 9999px;
```

### Shadows

```css
--shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
```

### Glassmorphism

```css
.glass-card {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}
```
