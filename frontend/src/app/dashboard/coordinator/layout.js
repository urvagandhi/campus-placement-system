import CoordinatorLayout from "@/components/layout/CoordinatorLayout";

export default function Layout({ children }) {
    return (
        <CoordinatorLayout>
            {children}
        </CoordinatorLayout>
    );
}
