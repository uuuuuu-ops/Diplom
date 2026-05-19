import React from 'react';
import { CourseDraft } from '../../types/createCourse';

const CATEGORIES = [
  'Programming','Mathematics','Physics','Sciences'
];

interface Props {
  draft: CourseDraft;
  updateDraft: (patch: Partial<CourseDraft>) => void;
  onNext: () => void;
}

const StepBasics: React.FC<Props> = ({ draft, updateDraft, onNext }) => {
  const handleThumbUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = ev => {
      if (ev.target?.result) updateDraft({ thumbnail: ev.target.result as string, thumbnailFile: file });
    };
    reader.readAsDataURL(file);
  };

  const charCount = (val: string, max: number) => `${val.length} / ${max}`;
  const charWarn  = (val: string, max: number) => val.length > max * 0.9;

  return (
    <>
      <div className="cc-card">
        <div className="cc-card-head">
          <div className="cc-card-head-left">
            <div>
              <div className="cc-card-label">Course identity</div>
              <div className="cc-card-desc">The first thing students will see</div>
            </div>
          </div>
        </div>

        <div className="cc-card-body">
          {/* Title */}
          <div className="cc-field">
            <label className="cc-label">
              Title <span className="cc-label-req">*</span>
            </label>
            <input
              className="cc-input"
              type="text"
              maxLength={120}
              placeholder="e.g. Complete Python Bootcamp: From Zero to Hero"
              value={draft.title}
              onChange={e => updateDraft({ title: e.target.value })}
            />
            <div className={`cc-char-count ${charWarn(draft.title, 120) ? 'cc-char-count--warn' : ''}`}>
              {charCount(draft.title, 120)}
            </div>
          </div>

          {/* Description */}
          <div className="cc-field">
            <label className="cc-label">
              Description <span className="cc-label-req">*</span>
            </label>
            <textarea
              className="cc-textarea"
              maxLength={800}
              placeholder="Tell students what they'll learn, who it's for, and what they'll build…"
              value={draft.description}
              onChange={e => updateDraft({ description: e.target.value })}
            />
            <div className={`cc-char-count ${charWarn(draft.description, 800) ? 'cc-char-count--warn' : ''}`}>
              {charCount(draft.description, 800)}
            </div>
          </div>

        </div>
      </div>

      <div className="cc-card">
        <div className="cc-card-head">
          <div className="cc-card-head-left">
            <div>
              <div className="cc-card-label">Thumbnail</div>
              <div className="cc-card-desc">Upload a custom image</div>
            </div>
          </div>
        </div>

        <div className="cc-card-body">
          <div className="cc-thumb-preview">
            {draft.thumbnail?.startsWith('data:') ? (
              <img
                src={draft.thumbnail}
                alt="Thumbnail preview"
                style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 14 }}
              />
            ) : (
              <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
                No image selected
              </span>
            )}
          </div>

          <div className="cc-upload" style={{ padding: '0.75rem 1rem' }}>
            <input type="file" accept="image/*" onChange={handleThumbUpload} />
            <span style={{ fontFamily: 'var(--font-mono)', fontSize: '0.85rem', color: 'var(--text-muted)', fontStyle: 'italic' }}>
              Upload custom image (JPG, PNG, WebP)
            </span>
          </div>
        </div>
      </div>

      <div className="cc-card">
        <div className="cc-card-head">
          <div className="cc-card-head-left">
            <div>
              <div className="cc-card-label">Settings</div>
              <div className="cc-card-desc">Category, level and duration</div>
            </div>
          </div>
        </div>

        <div className="cc-card-body">
          <div className="cc-grid-2" style={{ marginBottom: '1rem' }}>
            <div className="cc-field" style={{ marginBottom: 0 }}>
              <label className="cc-label">
                Category <span className="cc-label-req">*</span>
              </label>
              <select
                className="cc-select"
                value={draft.category}
                onChange={e => updateDraft({ category: e.target.value })}
              >
                <option value="">Choose…</option>
                {CATEGORIES.map(c => <option key={c}>{c}</option>)}
              </select>
            </div>

            <div className="cc-field" style={{ marginBottom: 0 }}>
              <label className="cc-label">Est. duration (hours)</label>
              <input
                className="cc-input"
                type="number"
                min={1}
                max={500}
                placeholder="e.g. 12"
                value={draft.duration}
                onChange={e => updateDraft({ duration: e.target.value })}
              />
            </div>
          </div>

          <div className="cc-field" style={{ marginBottom: 0 }}>
            <label className="cc-label">
              Level <span className="cc-label-req">*</span>
            </label>
            <div className="cc-level-picker">
              {(['BEGINNER', 'INTERMEDIATE', 'ADVANCED'] as const).map(lvl => (
                <div
                  key={lvl}
                  className={`cc-level-opt ${draft.level === lvl ? 'cc-level-opt--active' : ''}`}
                  onClick={() => updateDraft({ level: lvl })}
                >
                  {lvl.charAt(0) + lvl.slice(1).toLowerCase()}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="cc-actions">
        <div />
        <button className="cc-btn cc-btn--accent" onClick={onNext}>
          Lessons &amp; Quizzes →
        </button>
      </div>
    </>
  );
};

export default StepBasics;
