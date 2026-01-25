'use client';

import { Building2 } from 'lucide-react';
import Card from '@/components/ui/Card';

export default function CompanyDirectory() {
    return (
        <div className="space-y-6 animate-fade-in pb-10 flex items-center justify-center min-h-[60vh]">
            <Card className="max-w-md w-full text-center py-12 px-6 border-dashed border-2 border-gray-200 bg-gray-50/50">
                <div className="mx-auto w-16 h-16 bg-gray-100 rounded-2xl flex items-center justify-center mb-6">
                    <Building2 className="text-gray-400" size={32} />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 mb-2">Company Database</h2>
                <p className="text-gray-500 mb-6">
                    This feature is currently under active development. <br />
                    Check back soon for updates!
                </p>
                <div className="inline-flex items-center justify-center px-4 py-2 bg-amber-100/50 text-amber-700 text-sm font-bold rounded-lg border border-amber-200">
                    COMING SOON
                </div>
                {/* TODO: Implement Company Database fully with scoped access */}
            </Card>
        </div>
    );
}
